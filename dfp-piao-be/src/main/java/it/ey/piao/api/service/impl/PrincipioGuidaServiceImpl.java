package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.entity.PrincipioGuida;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.repository.IPrincipioGuidaRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IPrincipioGuidaService;
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
@Transactional
public class PrincipioGuidaServiceImpl implements IPrincipioGuidaService {

    private static final Logger log = LoggerFactory.getLogger(PrincipioGuidaServiceImpl.class);

    private final IPrincipioGuidaRepository principioGuidaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public PrincipioGuidaServiceImpl(IPrincipioGuidaRepository principioGuidaRepository,
                                     ApplicationEventPublisher eventPublisher,
                                     StoricoModificaHelper storicoModificaHelper,
                                     IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.principioGuidaRepository = principioGuidaRepository;
        this.eventPublisher = eventPublisher;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID del Principio Guida non può essere nullo");
        }

        try {
            Optional<PrincipioGuida> existing = principioGuidaRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un Principio Guida non esistente con id={}", id);
                throw new RuntimeException("Principio Guida non trovato con id: " + id);
            }

            PrincipioGuida principioGuida = existing.get();

            // Evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(PrincipioGuida.class, principioGuida));

            // Cancellazione
            LocalDateTime deactivationTime = LocalDateTime.now();

            principioGuidaRepository.softDeleteById(id,deactivationTime);

            // Salva storico modifica dopo la cancellazione
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null) {
                Long idSezione = (principioGuida.getSezione1() != null) ? principioGuida.getSezione1().getId() : null;
                if (idSezione != null) {
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
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                Long idSezione = (principioGuida.getSezione1() != null) ? principioGuida.getSezione1().getId() : null;
                if (idSezione != null) {
                    if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione, Sezione.SEZIONE_1.name())))) {
                        storicoStatoSezioneRepository.save(
                            StoricoStatoSezione.builder().statoSezione(
                                    StatoSezione.builder()
                                        .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                        .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                        .build())
                                .idEntitaFK(idSezione)
                                .codTipologiaFK(Sezione.SEZIONE_1.name())
                                .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                .createdByNameSurname(updatedByNameSurname)
                                .createdByRole(updatedByRole)
                                .build());
                    }
                }
            }

            // Evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(principioGuida));
            log.info("Principio Guida con id={} cancellato con successo", id);

        } catch (DataAccessException dae) {
            log.error("Errore DB nella cancellazione del Principio Guida id={}: {}", id, dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante la cancellazione del Principio Guida", dae);
        } catch (Exception e) {
            log.error("Errore inatteso nella cancellazione del Principio Guida id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione del Principio Guida", e);
        }
    }
}
