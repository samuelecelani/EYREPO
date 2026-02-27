package it.ey.piao.api.service.impl;


import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.AttivitaSensibileMapper;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAttivitaSensibileRepository;
import it.ey.piao.api.repository.ISezione23Repository;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IAttivitaSensibileService;
import it.ey.piao.api.service.IMisuraPrevenzioneEventoRischioService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttivitaSensibileService implements IAttivitaSensibileService {

    private static final Logger log = LoggerFactory.getLogger(AttivitaSensibileService.class);

    private final IAttivitaSensibileRepository attivitaSensibileRepository;
    private final AttivitaSensibileMapper attivitaSensibileMapper;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;
    private final ISezione23Repository sezione23Repository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final IAttoreRepository attoreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IMisuraPrevenzioneEventoRischioService misuraPrevenzioneEventoRischioService;


    public AttivitaSensibileService(IAttivitaSensibileRepository attivitaSensibileRepository,
                                    AttivitaSensibileMapper attivitaSensibileMapper,
                                    CommonMapper commonMapper,
                                    MongoUtils mongoUtils,
                                    ISezione23Repository sezione23Repository,
                                    IUlterioriInfoRepository ulterioriInfoRepository,
                                    IAttoreRepository attoreRepository,
                                    ApplicationEventPublisher eventPublisher, IMisuraPrevenzioneEventoRischioService misuraPrevenzioneEventoRischioService) {
        this.attivitaSensibileRepository = attivitaSensibileRepository;
        this.attivitaSensibileMapper = attivitaSensibileMapper;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
        this.sezione23Repository = sezione23Repository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.attoreRepository = attoreRepository;
        this.eventPublisher = eventPublisher;
        this.misuraPrevenzioneEventoRischioService = misuraPrevenzioneEventoRischioService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(AttivitaSensibileDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // DTO in entity JPA
            AttivitaSensibile entity = attivitaSensibileMapper.toEntity(dto, context);

            // Relazione con Sezione23
            entity.setSezione23(sezione23Repository.getReferenceById(dto.getIdSezione23()));

            // Salvo lo stato dell'entity per eventuale rollback
            if (entity.getId() != null) {
                attivitaSensibileRepository.findById(entity.getId())
                    .ifPresent(existing ->
                        eventPublisher.publishEvent(new BeforeUpdateEvent<>(AttivitaSensibile.class, existing))
                    );
            }

            // Salvo l'entity principale nel DB relazionale
            AttivitaSensibile savedEntity = attivitaSensibileRepository.save(entity);



            // Salvo i dati MongoDB
            saveMongoData(dto);

            // Pubblico successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedEntity));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate per AttivitaSensibile id={}: {}", dto.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(attivitaSensibileMapper.toEntity(dto, new CycleAvoidingMappingContext()), e));
            throw new RuntimeException("Errore durante il save o update dell'Attivita Sensibile", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdateAll(List<AttivitaSensibileDTO> attivitaSensibili) {
        if (attivitaSensibili == null || attivitaSensibili.isEmpty()) {
            log.debug("Lista attività sensibili vuota o null, skip salvataggio batch");
            return;
        }

        log.info("Salvataggio batch di {} attività sensibili", attivitaSensibili.size());

        try {

                attivitaSensibili.forEach(attivitaSensibile -> {
                    if (attivitaSensibile.getEventoRischio() != null) {
                        attivitaSensibile.getEventoRischio().forEach(eventoRischio -> {
                            if (eventoRischio.getMisure() != null) {
                                misuraPrevenzioneEventoRischioService.saveOrUpdateAll(eventoRischio.getMisure());
                            }
                        });

                    }
                });



        } catch (Exception e) {
            log.error("Errore durante il salvataggio batch delle attività sensibili: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch delle attività sensibili", e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<AttivitaSensibileDTO> getAllBySezione23(Long idSezione23) {
        if (idSezione23 == null) {
            throw new IllegalArgumentException("L'ID della Sezione23 non può essere nullo");
        }

        List<AttivitaSensibileDTO> response;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupero il riferimento alla Sezione23 senza eseguire subito la query
            Sezione23 sezione23 = sezione23Repository.getReferenceById(idSezione23);

            // Recupero tutte le Attività Sensibili associate alla Sezione23
            List<AttivitaSensibile> entities = attivitaSensibileRepository.getAttivitaSensibileBySezione23(sezione23);

            // Mapping Entity → DTO con arricchimento dati Mongo
            response = entities.stream()
                .map(entity -> {
                    AttivitaSensibileDTO attDTO = attivitaSensibileMapper.toDto(entity, context);
                    return loadMongoData(attDTO);
                })
                .toList();

            // Pubblico evento di successo della transazione
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante il recupero delle Attività Sensibili per Sezione23 id={}: {}",
                idSezione23, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(AttivitaSensibile.class, e));
            throw new RuntimeException("Errore durante il recupero delle Attività Sensibili per Sezione 23", e);
        }

        return response;
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID dell'Attivita Sensibile non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<AttivitaSensibile> existing = attivitaSensibileRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un'Attivita Sensibile non esistente con id={}", id);
                throw new RuntimeException("Attivita Sensibile non trovata con id: " + id);
            }

            // evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(AttivitaSensibile.class, existing.get()));

            // Cancellazione da Postgres
            attivitaSensibileRepository.deleteById(id);

            // Propagazione della cancellazione su MongoDB
            ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_23_ATTIVITASENSIBILE);
            attoreRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_23_ATTIVITASENSIBILE);

            // evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("Attivita Sensibile con id={} cancellata con successo", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione dell'Attivita Sensibile id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new AttivitaSensibile(), e));
            throw new RuntimeException("Errore durante la cancellazione dell'Attivita Sensibile", e);
        }
    }

    /**
     * Carica i dati MongoDB per un AttivitaSensibileDTO.
     * Recupera UlterioriInfo e Attore associati.
     */
    @Override
    @Transactional(readOnly = true)
    public AttivitaSensibileDTO loadMongoData(AttivitaSensibileDTO attivitaSensibile) {
        if (attivitaSensibile == null || attivitaSensibile.getId() == null) {
            log.warn("AttivitaSensibileDTO o ID è null, skip caricamento MongoDB");
            return attivitaSensibile;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Caricamento UlterioriInfo da MongoDB
            attivitaSensibile.setUlterioriInfo(
                Optional.ofNullable(
                        ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                            attivitaSensibile.getId(),
                            Sezione.SEZIONE_23_ATTIVITASENSIBILE
                        )
                    )
                    .map(info -> commonMapper.ulterioriInfoEntityToDto(info, context))
                    .orElse(null)
            );

            // Caricamento Attore da MongoDB (solo per externalId univoco)
            Attore attore = attoreRepository.findAllByExternalIdAndTipoSezione(
                attivitaSensibile.getId(),
                Sezione.SEZIONE_23_ATTIVITASENSIBILE
            );
            attivitaSensibile.setAttore(
                Optional.ofNullable(attore)
                    .map(a -> commonMapper.attoreEntityToDto(a, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB caricati per AttivitaSensibile id={}", attivitaSensibile.getId());
            return attivitaSensibile;

        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per AttivitaSensibile id={}: {}",
                attivitaSensibile.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB AttivitaSensibile", e);
        }
    }

    /**
     * Salva i dati MongoDB per un AttivitaSensibileDTO.
     * Salva UlterioriInfo e Attore associati.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoData(AttivitaSensibileDTO request) {
        if (request == null || request.getId() == null) {
            log.warn("Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo MongoDB
            if (request.getUlterioriInfo() != null) {
                UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(request.getUlterioriInfo(), context);
                entityMongo.setExternalId(request.getId());

                mongoUtils.saveItem(
                    entityMongo,
                    ulterioriInfoRepository,
                    UlterioriInfo.class,
                    en -> en.setTipoSezione(Sezione.SEZIONE_23_ATTIVITASENSIBILE)
                );
            }

            // Salva Attore MongoDB SENZA idPiao (ogni AttivitaSensibile ha il proprio Attore)
            if (request.getAttore() != null) {
                Attore entityMongo = commonMapper.attoreDtoToEntity(request.getAttore(), context);
                entityMongo.setExternalId(request.getId()); // Solo externalId = ID AttivitaSensibile

                mongoUtils.saveItem(
                    entityMongo,
                    attoreRepository,
                    Attore.class,
                    en -> en.setTipoSezione(Sezione.SEZIONE_23_ATTIVITASENSIBILE)
                );
            }

            log.debug("Dati MongoDB salvati per AttivitaSensibile id={}", request.getId());

        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per AttivitaSensibile id={}: {}",
                request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB AttivitaSensibile", e);
        }
    }

    /**
     * Salva i dati MongoDB per una lista di AttivitaSensibileDTO in batch.
     * Ottimizzato: recupera idPiao una sola volta e salva in batch.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoDataBatch(List<AttivitaSensibileDTO> requests) {
        if (requests == null || requests.isEmpty()) {
            log.debug("Lista requests vuota, skip salvataggio MongoDB batch");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Preparo liste per batch save
            List<UlterioriInfo> ulterioriInfoList = new ArrayList<>();
            List<Attore> attoreList = new ArrayList<>();

            for (AttivitaSensibileDTO request : requests) {
                if (request == null || request.getId() == null) {
                    continue;
                }

                // Prepara UlterioriInfo
                if (request.getUlterioriInfo() != null) {
                    UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(request.getUlterioriInfo(), context);
                    entityMongo.setExternalId(request.getId());
                    entityMongo.setTipoSezione(Sezione.SEZIONE_23_ATTIVITASENSIBILE);
                    ulterioriInfoList.add(entityMongo);
                }

                // Prepara Attore SENZA idPiao (ogni AttivitaSensibile ha il proprio)
                if (request.getAttore() != null) {
                    Attore entityMongo = commonMapper.attoreDtoToEntity(request.getAttore(), context);
                    entityMongo.setExternalId(request.getId()); // Solo externalId
                    entityMongo.setTipoSezione(Sezione.SEZIONE_23_ATTIVITASENSIBILE);
                    attoreList.add(entityMongo);
                }
            }

            // Batch save su MongoDB
            if (!ulterioriInfoList.isEmpty()) {
                ulterioriInfoRepository.saveAll(ulterioriInfoList);
                log.debug("Salvate {} UlterioriInfo su MongoDB", ulterioriInfoList.size());
            }
            if (!attoreList.isEmpty()) {
                attoreRepository.saveAll(attoreList);
                log.debug("Salvati {} Attori su MongoDB", attoreList.size());
            }

            log.info("Batch salvataggio MongoDB completato per {} AttivitàSensibili", requests.size());

        } catch (Exception e) {
            log.error("Errore batch salvataggio MongoDB AttivitaSensibili: {}", e.getMessage(), e);
            throw new RuntimeException("Errore batch salvataggio MongoDB AttivitaSensibili", e);
        }
    }

}
