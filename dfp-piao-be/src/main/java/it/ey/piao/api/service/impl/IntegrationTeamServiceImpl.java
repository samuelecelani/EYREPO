package it.ey.piao.api.service.impl;


import it.ey.dto.BaseDTO;
import it.ey.entity.IntegrationTeam;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.repository.IIntegrationTeamRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IIntegrationTeamService;
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
import java.util.Optional;

@Service
public class IntegrationTeamServiceImpl implements IIntegrationTeamService {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTeamServiceImpl.class);


    private final IIntegrationTeamRepository integrationTeamRepository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;




    public IntegrationTeamServiceImpl(IIntegrationTeamRepository integrationTeamRepository, StoricoModificaHelper storicoModificaHelper, ApplicationEventPublisher eventPublisher, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.integrationTeamRepository = integrationTeamRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.eventPublisher = eventPublisher;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional
    public void deleteById(     Long id,
                                String campiModificati,
                                Long idPiao,
                                String testoSezione,
                                String updatedByNameSurname,
                                String updatedByRole,
                                String statoSezione) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID dell'IntegrationTeam non può essere nullo");
        }

        try {
            Optional<IntegrationTeam> existing = integrationTeamRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un IntegrationTeam non esistente con id={}", id);
                throw new RuntimeException("IntegrationTeam non trovato con id: " + id);
            }

            IntegrationTeam integrationTeam = existing.get();

            // Evento prima della "cancellazione" (soft delete)
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(IntegrationTeam.class, integrationTeam));

            // Soft delete: active=false + deactivationTime=now
            LocalDateTime deactivationTime = LocalDateTime.now();
            integrationTeamRepository.softDeleteById(id, deactivationTime);

            // idSezione necessario per storico (si ricava dalla relazione)
            Long idSezione = (integrationTeam.getSezione1() != null) ? integrationTeam.getSezione1().getId() : null;

            // 1) Storico modifica (campiModificati) se presente
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null && idSezione != null) {

                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(dto, idSezione, idPiao, Sezione.SEZIONE_1);
            }

            // 2) Storico stato sezione dopo la delete (se mi arriva statoSezione)
            if (statoSezione != null && !statoSezione.isBlank() && idSezione != null) {

                String statoDb = StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione, Sezione.SEZIONE_1.name())
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
                            .codTipologiaFK(Sezione.SEZIONE_1.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build()
                    );
                }
            }

            // Evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(integrationTeam));
            log.info("IntegrationTeam con id={} soft-deletato con successo", id);

        } catch (DataAccessException dae) {
            log.error("Errore DB nella cancellazione IntegrationTeam id={}: {}", id, dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante la cancellazione IntegrationTeam", dae);

        } catch (Exception e) {
            log.error("Errore inatteso nella cancellazione IntegrationTeam id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione IntegrationTeam", e);
        }
    }
}
