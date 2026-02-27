package it.ey.worker;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.ey.worker.config.ConsumerProperties;
import it.ey.worker.dto.NotificationDTO;
import it.ey.worker.notification.INotification;
import it.ey.worker.notification.NotificationFactory;
import it.ey.worker.service.INotificationService;
import it.ey.worker.utils.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Consumer dedicato alla coda PDF.
 * Legge dalla coda "PDF" e delega alla NotificationFactory
 * che istanzia PdfNotification per l'elaborazione.
 *
 * Flusso:
 *   Notifica_BE (PdfProducer) → Queue "PDF"
 *                                    ↓
 *                   WORKER (PdfConsumer): Factory → PdfNotification → genera PDF + salva su DB
 */
@Component
public class PdfConsumer {

    private static final Logger log = LoggerFactory.getLogger(PdfConsumer.class);

    private final ConsumerProperties consumerProperties;
    private final INotificationService notificationService;

    public PdfConsumer(ConsumerProperties consumerProperties,
                       INotificationService notificationService) {
        this.consumerProperties = consumerProperties;
        this.notificationService = notificationService;
    }

    /**
     * Listener sulla coda PDF (ANYCAST point-to-point).
     * La factory seleziona automaticamente PdfNotification in base al type.
     */
    @JmsListener(destination = "#{consumerProperties.pdfDestination}", containerFactory = "queueListenerFactory")
    public void onPdfMessage(String msg) {
        try {
            NotificationDTO notificationDTO = WorkerUtil.getObject(msg, NotificationDTO.class);
            if (notificationDTO != null) {
                log.info("Ricevuto messaggio PDF dalla coda, modulo={}", notificationDTO.getIdModulo());

                INotification notification = NotificationFactory.getNotificationByType(
                    notificationDTO.getType().name(), notificationService);

                NotificationDTO saved = notification.sendNotification(notificationDTO);
                log.info("Elaborazione PDF completata con successo, id={}",
                    saved != null ? saved.getId() : "?");
            }
        } catch (JsonProcessingException e) {
            log.error("Errore deserializzazione messaggio PDF: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Errore elaborazione messaggio PDF: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
