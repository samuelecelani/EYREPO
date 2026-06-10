package it.ey.piao.bff.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.ey.dto.AllegatoDTO;
import it.ey.dto.ExcelNotificationDTO;
import it.ey.dto.NotificationDTO;
import it.ey.dto.PdfNotificationDTO;
import it.ey.enums.StatusAllegato;
import it.ey.piao.bff.service.IAllegatoService;
import it.ey.piao.bff.service.NotificationSinkService;
import it.ey.piao.bff.service.S3Service;
import it.ey.utils.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Listener JMS del BFF: ascolta il Topic dedicato "eventiProcessati".
 * Riceve SOLO i messaggi pubblicati dal WORKER dopo il salvataggio su DB avvenuto con successo.
 *
 * Flusso:
 *   Notifica_BE → Topic "eventi"
 *                       ↓
 *                   WORKER: salva su DB → pubblica su Topic "eventiProcessati"
 *                                                    ↓
 *                       BFF (listener su "eventiProcessati") → Sink → SSE → FE
 */
@Component
public class NotificationJmsListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationJmsListener.class);

    private final NotificationSinkService sinkService;
    private final IAllegatoService allegatoService;
    private final S3Service s3Service;

    @Autowired
    public NotificationJmsListener(NotificationSinkService sinkService,
                                   IAllegatoService allegatoService,
                                   S3Service s3Service) {
        this.sinkService = sinkService;
        this.allegatoService = allegatoService;
        this.s3Service = s3Service;
    }

    /**
     * Ascolta il Topic dedicato "eventiProcessati".
     * Nessun selector necessario: solo il WORKER scrive su questo Topic.
     */
    @JmsListener(
        destination = "#{consumerProperties.confirmationTopic}",
        containerFactory = "topicListenerFactory"
    )
    public void onNotificationConfirmed(String msg) {
        log.info("BFF: ricevuto messaggio confermato dal WORKER su Topic eventiProcessati");
        try {
            // Prova a deserializzare come ExcelNotificationDTO (ha campi specifici come generatedExcelS3Key)
            try {
                ExcelNotificationDTO excelNotification = WorkerUtil.getObject(msg, ExcelNotificationDTO.class);
                if (excelNotification != null && excelNotification.getAllegato() != null) {
                    log.info("BFF: rilevato ExcelNotificationDTO");
                    handleAllegatoUpdate(excelNotification.getAllegato());
                    // Genera presigned URL e settala sulla notification per il FE
                    String s3Key = excelNotification.getGeneratedExcelS3Key();
                    String presignedUrl = generatePresignedUrl(s3Key, excelNotification.getAllegato());
                    excelNotification.setDownloadUrl(presignedUrl);
                    sinkService.emit(excelNotification);
                    return;
                }
            } catch (JsonProcessingException e) {
                log.debug("BFF: messaggio non è ExcelNotificationDTO, provo PdfNotificationDTO: {}", e.getMessage());
            }

            // Prova a deserializzare come PdfNotificationDTO
            try {
                PdfNotificationDTO pdfNotification = WorkerUtil.getObject(msg, PdfNotificationDTO.class);
                if (pdfNotification != null && pdfNotification.getAllegato() != null) {
                    log.info("BFF: rilevato PdfNotificationDTO");
                    handleAllegatoUpdate(pdfNotification.getAllegato());
                    // Genera presigned URL e settala sulla notification per il FE
                    String s3Key = pdfNotification.getGeneratedPdfS3Key();
                    String presignedUrl = generatePresignedUrl(s3Key, pdfNotification.getAllegato());
                    pdfNotification.setDownloadUrl(presignedUrl);
                    sinkService.emit(pdfNotification);
                    return;
                }
            } catch (JsonProcessingException e) {
                log.debug("BFF: messaggio non è PdfNotificationDTO, provo NotificationDTO: {}", e.getMessage());
            }

            // Fallback: NotificationDTO standard
            NotificationDTO notificationDTO = WorkerUtil.getObject(msg, NotificationDTO.class);
            if (notificationDTO != null) {
                log.info("BFF: emit SSE per modulo '{}' (salvataggio confermato)", notificationDTO.getIdModulo());
                sinkService.emit(notificationDTO);
            }
        } catch (JsonProcessingException e) {
            log.error("BFF: errore deserializzazione messaggio confermato: {}", e.getMessage(), e);
        } catch (java.io.IOException e) {
            log.info("BFF: client SSE disconnesso durante l'emit (connessione chiusa dal FE): {}", e.getMessage());
        } catch (Exception e) {
            log.error("BFF: errore generico: {}", e.getMessage(), e);
        }
    }

    /**
     * Gestisce l'aggiornamento/salvataggio dell'allegato su DB (comune a PDF e Excel).
     */
    private void handleAllegatoUpdate(AllegatoDTO allegato) {
        Long allegatoId = allegato.getId();
        log.info("BFF: rilevato NotificationDTO con allegato id={}, verifico stato prima di aggiornare", allegatoId);

        if (allegatoId != null) {
            try {
                AllegatoDTO existing = allegatoService.findAllegatoById(allegatoId).block();
                if (existing != null) {
                    String stato = existing.getStatusAllegato();
                    StatusAllegato statusEnum = StatusAllegato.valueOf(stato);
                    if (StatusAllegato.GENERATO.equals(statusEnum)) {
                        log.info("BFF: allegato id={} già in stato '{}', skip update", allegatoId, stato);
                    } else if (StatusAllegato.ERRORE_GENERAZIONE.equals(statusEnum)) {
                        // Allegato in errore: lo eliminiamo SOLO dal DB (no S3, il file non è stato generato)
                        log.info("BFF: allegato id={} in stato 'ERRORE_GENERAZIONE', elimino dal DB per permettere la rigenerazione", allegatoId);
                        String fileKey = existing.getCodDocumento();
                        allegatoService.deleteAllegato(
                            allegatoId,
                            (fileKey != null && !fileKey.isBlank()) ? fileKey : "n/a",
                            false, // isDoc=false -> NON tentare la delete su S3 (il file non esiste)
                            null,
                            null,
                            existing.getCodTipologiaFK(),
                            null
                        ).block();
                        log.info("BFF: allegato id={} eliminato dal DB, pronto per la rigenerazione", allegatoId);
                    } else {
                        log.info("BFF: allegato id={} in stato '{}', procedo con update sincrono", allegatoId, stato);
                        allegatoService.updateAllegato(allegato).block();
                        log.info("BFF: allegato id={} aggiornato con successo", allegatoId);
                    }
                } else {
                    log.info("BFF: allegato id={} non trovato su DB, salvataggio come nuovo", allegatoId);
                    allegatoService.saveAllegatoSenzaUpload(allegato).block();
                    log.info("BFF: allegato id={} salvato con successo", allegatoId);
                }
            } catch (Exception e) {
                log.error("BFF: errore gestione allegato id={}: {}", allegatoId, e.getMessage(), e);
            }
        } else {
            log.info("BFF: allegato senza ID, salvataggio diretto");
            try {
                allegatoService.saveAllegatoSenzaUpload(allegato).block();
                log.info("BFF: allegato salvato con successo");
            } catch (Exception e) {
                log.error("BFF: errore salvataggio allegato: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Genera un presigned URL S3 per il download del file generato.
     * Usa la s3Key esplicita (generatedPdfS3Key / generatedExcelS3Key),
     * altrimenti fallback su codDocumento dell'allegato.
     *
     * @return presigned URL oppure null se non generabile
     */
    private String generatePresignedUrl(String s3Key, AllegatoDTO allegato) {
        String fileKey = (s3Key != null && !s3Key.isBlank()) ? s3Key : allegato.getCodDocumento();

        if (fileKey == null || fileKey.isBlank()) {
            log.warn("BFF: nessuna fileKey disponibile per allegato id={}, skip presigned URL", allegato.getId());
            return null;
        }

        try {
            String presignedUrl = s3Service.generatePresignedUrl(fileKey).block();
            if (presignedUrl != null) {
                log.info("BFF: presigned URL generata per allegato id={}, fileKey={}", allegato.getId(), fileKey);
            } else {
                log.warn("BFF: presigned URL null per allegato id={}, fileKey={}", allegato.getId(), fileKey);
            }
            return presignedUrl;
        } catch (Exception e) {
            log.error("BFF: errore generazione presigned URL per allegato id={}, fileKey={}: {}",
                allegato.getId(), fileKey, e.getMessage(), e);
            return null;
        }
    }
}
