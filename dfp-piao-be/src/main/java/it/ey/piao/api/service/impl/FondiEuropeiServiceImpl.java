package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.entity.FondiEuropei;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.repository.IFondiEuropeiRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IFondiEuropeiService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FondiEuropeiServiceImpl implements IFondiEuropeiService {

    private static final Logger log = LoggerFactory.getLogger(FondiEuropeiServiceImpl.class);

    private final IFondiEuropeiRepository fondiEuropeiRepository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public FondiEuropeiServiceImpl(IFondiEuropeiRepository fondiEuropeiRepository, StoricoModificaHelper storicoModificaHelper, ApplicationEventPublisher eventPublisher, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.fondiEuropeiRepository = fondiEuropeiRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.eventPublisher = eventPublisher;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }


    @Override
    @Transactional
    public void deleteById(Long id,
                           String campiModificati,
                           Long idPiao,
                           String testoSezione,
                           String updatedByNameSurname,
                           String updatedByRole,
                           String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("L'ID del Fondo Europeo non può essere nullo");
        }

        try {
            var existing = fondiEuropeiRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un Fondo Europeo non esistente con id={}", id);
                throw new RuntimeException("Fondo Europeo non trovato con id: " + id);
            }

            FondiEuropei fondiEuropei = existing.get();

            // Evento prima della cancellazione (soft delete)
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(FondiEuropei.class, fondiEuropei));

            LocalDateTime deactivationTime = LocalDateTime.now();
            fondiEuropeiRepository.softDeleteById(id, deactivationTime);

            // idSezione (Sezione21) per storico
            Long idSezione = (fondiEuropei.getSezione21() != null) ? fondiEuropei.getSezione21().getId() : null;

            // Storico modifica
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null && idSezione != null) {

                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(dto, idSezione, idPiao, Sezione.SEZIONE_21);
            }

            // Storico stato sezione
            if (statoSezione != null && !statoSezione.isBlank() && idSezione != null) {

                String statoDb = StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione, Sezione.SEZIONE_21.name())
                );

                String statoReq = StatoEnum.fromDescrizione(statoSezione).name();

                if (!statoReq.equals(statoDb)) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder()
                            .statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build()
                            )
                            .idEntitaFK(idSezione)
                            .codTipologiaFK(Sezione.SEZIONE_21.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build()
                    );
                }
            }

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(fondiEuropei));
            log.info("Fondo Europeo con id={} soft-deletato con successo", id);

        } catch (DataAccessException dae) {
            log.error("Errore DB nella cancellazione Fondo Europeo id={}: {}", id, dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante la cancellazione del Fondo Europeo", dae);

        } catch (Exception e) {
            log.error("Errore inatteso nella cancellazione Fondo Europeo id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione del Fondo Europeo", e);
        }
    }
}
