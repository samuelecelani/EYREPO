package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.entity.OrganoPolitico;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.repository.IOrganoPoliticoRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IOrganoPoliticoService;
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
public class OrganoPoliticoServiceImpl implements IOrganoPoliticoService {

    private static final Logger log = LoggerFactory.getLogger(OrganoPoliticoServiceImpl.class);

    private final IOrganoPoliticoRepository organoPoliticoRepository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public OrganoPoliticoServiceImpl(IOrganoPoliticoRepository organoPoliticoRepository, StoricoModificaHelper storicoModificaHelper, ApplicationEventPublisher eventPublisher, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.organoPoliticoRepository = organoPoliticoRepository;
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
            throw new IllegalArgumentException("L'ID dell'Organo Politico non può essere nullo");
        }

        try {
            Optional<OrganoPolitico> existing = organoPoliticoRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un Organo Politico non esistente con id={}", id);
                throw new RuntimeException("Organo Politico non trovato con id: " + id);
            }

            OrganoPolitico organoPolitico = existing.get();

            // Evento prima della "cancellazione" (soft delete)
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(OrganoPolitico.class, organoPolitico));

            // Soft delete: active=false + deactivationTime=now
            LocalDateTime deactivationTime = LocalDateTime.now();
            organoPoliticoRepository.softDeleteById(id, deactivationTime);

            // idSezione necessario per storico (si ricava dalla relazione)
            Long idSezione = (organoPolitico.getSezione1() != null) ? organoPolitico.getSezione1().getId() : null;

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
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(organoPolitico));
            log.info("Organo Politico con id={} soft-deletato con successo", id);

        } catch (DataAccessException dae) {
            log.error("Errore DB nella cancellazione Organo Politico id={}: {}", id, dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante la cancellazione Organo Politico", dae);

        } catch (Exception e) {
            log.error("Errore inatteso nella cancellazione Organo Politico id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione Organo Politico", e);
        }
    }
}
