package it.ey.piao.api.service.impl;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione22DTO;
import it.ey.dto.Sezione23DTO;
import it.ey.dto.Sezione331DTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione331Mapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.ISezione331Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.ISezione331Service;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class Sezione331ServiceImpl implements ISezione331Service {
    private final ApplicationEventPublisher eventPublisher;
    private final ISezione331Repository sezione331Repository;
    private final Sezione331Mapper sezione331Mapper;
    private static final Logger log = LoggerFactory.getLogger(Sezione331ServiceImpl.class);
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final PiaoMapper piaoMapper;

    public Sezione331ServiceImpl(ISezione331Repository sezione331Repository,
                                 Sezione331Mapper sezione331Mapper,
                                 ApplicationEventPublisher eventPublisher,
                                 IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                 PiaoMapper piaoMapper) {
        this.sezione331Repository = sezione331Repository;
        this.sezione331Mapper = sezione331Mapper;
        this.eventPublisher = eventPublisher;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.piaoMapper = piaoMapper;
    }



    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione331DTO getOrCreateSezione331(PiaoDTO piaoDTO) {
        if (piaoDTO == null || piaoDTO.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();


        try {
            // Recupera Sezione331 dal repository usando idPiao
            Sezione331 existing = sezione331Repository.findByIdPiao(piaoDTO.getId());

            if (existing != null) {
                Sezione331DTO response = sezione331Mapper.toDto(existing,context);
                response.setStatoSezione(
                    StoricoStatoSezioneUtils.getStato(
                        storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_331.name())
                    )
                );
                log.info("Sezione331 trovata per PIAO id={}", piaoDTO.getId());
                return response;
            }

            // Creazione nuova Sezione331 se non esiste
            log.info("Nessuna Sezione331 trovata per PIAO id={}, creazione nuova...", piaoDTO.getId());
            Sezione331 nuova = Sezione331.builder()
                .piao(Piao.builder().id(piaoDTO.getId()).build())
                .build();

            Sezione331 salvata = sezione331Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .codTipologiaFK(Sezione.SEZIONE_331.name())
                    .statoSezione(StatoSezione.builder()
                        .id(StatoEnum.DA_COMPILARE.getId())
                        .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                        .build())
                    .idEntitaFK(salvata.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build()
            );

            Sezione331DTO response = sezione331Mapper.toDto(salvata, context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione331 per PIAO id={}: {}", piaoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione331", e);
        }
    }







    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveOrUpdate(Sezione331DTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Mappo DTO -> Entity
            Sezione331 entity = sezione331Mapper.toEntity(request, context);

            // Evento rollback in caso di update
            if (entity.getId() != null) {
                sezione331Repository.findById(entity.getId())
                    .ifPresent(existing -> eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione331.class, existing)));
            }

            // Stato
            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());

            // Salvataggio principale
            Sezione331 savedEntity = sezione331Repository.save(entity);

            // Storico stato
            String statoCorrente = savedEntity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getId(), Sezione.SEZIONE_331.name())
            )
                : null;

            if (!statoEnum.name().equals(statoCorrente)) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(StatoSezione.builder()
                            .id(statoEnum.getId())
                            .testo(statoEnum.getDescrizione())
                            .build())
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_331.name())
                        .testo(statoEnum.getDescrizione())
                        .build()
                );
            }

            // MapStruct per evento
            Sezione331DTO savedDto = sezione331Mapper.toDto(savedEntity, context);

            // Evento successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedDto));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione331 per id={}: {}", request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(sezione331Mapper.toEntity(request, context), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione331", e);
        }
    }





    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione331DTO richiediValidazione(Long id) {
        log.info("Richiesta validazione stato Sezione331 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione331 entity = sezione331Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione331 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione331 saved = sezione331Repository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(

                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_331.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build()
            );

            Sezione331DTO response = sezione331Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione331 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione331", e);
        }
    }





    @Transactional(readOnly = true)
    @Override
    public Sezione331DTO findByPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione331 existing = sezione331Repository.findByIdPiao(idPiao);
            if (existing != null) {
                // Usa MapStruct per mappare correttamente tutte le relazioni annidate
                Sezione331DTO response = sezione331Mapper.toDto(existing, context);

                // Imposta lo stato corretto
                response.setStatoSezione(
                    StoricoStatoSezioneUtils.getStato(
                        storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_331.name())
                    )
                );

                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione331 per PIAO id={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione331", e);
        }
    }





}
