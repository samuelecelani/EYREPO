package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.ProceduraDTO;
import it.ey.entity.Procedura;
import it.ey.entity.Sezione21;
import it.ey.entity.StatoSezione;
import it.ey.entity.StoricoStatoSezione;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.ProceduraMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IProceduraRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IProceduraService;
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
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProceduraServiceImpl implements IProceduraService {

    private static final Logger log = LoggerFactory.getLogger(ProceduraServiceImpl.class);


    private final IProceduraRepository proceduraRepository;
    private final ProceduraMapper proceduraMapper;

    private final ApplicationEventPublisher eventPublisher;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;


    public ProceduraServiceImpl (IProceduraRepository proceduraRepository, ProceduraMapper proceduraMapper, ApplicationEventPublisher eventPublisher, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository){
        this.proceduraRepository = proceduraRepository;
        this.proceduraMapper = proceduraMapper;
        this.eventPublisher = eventPublisher;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    public List<ProceduraDTO> getProcedure(Long idSezione21) {
        try {
            return proceduraMapper.toDtoList(
                proceduraRepository.getProcedureBySezione21(Sezione21.builder().id(idSezione21).build())
                ,new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in recupero (Procedura): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il recupero dello Procedura", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in recupero (Procedura): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dello Procedura", e);
        }

    }

    @Override
    public ProceduraDTO save(ProceduraDTO request) {
        try {
            Procedura entity = proceduraMapper.toEntity(request,new CycleAvoidingMappingContext());
            Procedura saved = proceduraRepository.save(entity);
            return proceduraMapper.toDto(saved,new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in save (Procedura): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello Procedura", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (Procedura): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello Procedura", e);
        }
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
            throw new IllegalArgumentException("L'ID della Procedura non può essere nullo");
        }

        try {
            Optional<Procedura> existing = proceduraRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare una Procedura non esistente con id={}", id);
                throw new RuntimeException("Procedura non trovata con id: " + id);
            }

            Procedura procedura = existing.get();

            // Evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(Procedura.class, procedura));

            // Soft delete puntuale
            LocalDateTime deactivationTime = LocalDateTime.now();
            proceduraRepository.softDeleteById(id, deactivationTime);

            // idSezione necessario per storico
            Long idSezione = (procedura.getSezione21() != null) ? procedura.getSezione21().getId() : null;

            // Storico modifica dopo la cancellazione
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

            //  Storico stato sezione dopo la cancellazione
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

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(procedura));
            log.info("Procedura con id={} soft-deletata con successo", id);

        } catch (DataAccessException dae) {
            log.error("Errore DB nella cancellazione Procedura id={}: {}", id, dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante la cancellazione della Procedura", dae);

        } catch (Exception e) {
            log.error("Errore inatteso nella cancellazione Procedura id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione della Procedura", e);
        }
    }

}
