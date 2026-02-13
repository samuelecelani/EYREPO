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
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttivitaSensibileService  implements IAttivitaSensibileService {

    private final IAttivitaSensibileRepository attivitaSensibileRepository;
    private final AttivitaSensibileMapper attivitaSensibileMapper;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;
    private final ISezione23Repository sezione23Repository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final IAttoreRepository attoreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger log = LoggerFactory.getLogger(AttivitaSensibileService.class);

    public AttivitaSensibileService(IAttivitaSensibileRepository attivitaSensibileRepository, AttivitaSensibileMapper attivitaSensibileMapper, CommonMapper commonMapper, MongoUtils mongoUtils, ISezione23Repository sezione23Repository, IUlterioriInfoRepository ulterioriInfoRepository, IAttoreRepository attoreRepository, ApplicationEventPublisher eventPublisher) {
        this.attivitaSensibileRepository = attivitaSensibileRepository;
        this.attivitaSensibileMapper = attivitaSensibileMapper;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
        this.sezione23Repository = sezione23Repository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.attoreRepository = attoreRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AttivitaSensibileDTO saveOrUpdate(AttivitaSensibileDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        AttivitaSensibileDTO response;
        try {
            // DTO in entity JPA
            AttivitaSensibile entity = attivitaSensibileMapper.toEntity(dto);

            //   relazione con Sezione23
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

            //  Mappo l'entity salvata in DTO di risposta
            response = attivitaSensibileMapper.toDto(savedEntity);


            //  UlterioriInfo in Mongo (oggetto singolo)
            if (dto.getUlterioriInfo() != null) {
                UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(dto.getUlterioriInfo(), new CycleAvoidingMappingContext());
                entityMongo.setExternalId(savedEntity.getId());

                UlterioriInfo savedInfo = mongoUtils.saveItem(
                    entityMongo,
                    ulterioriInfoRepository,
                    UlterioriInfo.class,
                    en -> en.setTipoSezione(Sezione.SEZIONE_23_ATTIVITASENSIBILE)
                );

                response.setUlterioriInfo(commonMapper.ulterioriInfoEntityToDto(savedInfo, new CycleAvoidingMappingContext()));
            }


            //Attore in Mongo (oggetto singolo)
            if (dto.getAttore() != null) {
                Attore entityMongo = attivitaSensibileMapper.attoreToEntity(dto.getAttore());
                entityMongo.setExternalId(savedEntity.getSezione23().getPiao().getId());

                Attore savedInfo = mongoUtils.saveItem(
                    entityMongo,
                    attoreRepository,
                    Attore.class,
                    en -> en.setTipoSezione(Sezione.PIAO)
                );

                response.setAttore(attivitaSensibileMapper.attoreToDto(savedInfo));
            }




            // Pubblico  successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            //  Log dell'errore e pubblicazione evento di fallimento
            log.error("Errore durante Save o update per fase id={}: {}", dto.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(attivitaSensibileMapper.toEntity(dto), e));
            throw new RuntimeException("Errore durante il save o update dell'Attivita Sensibile", e);
        }

        // Ritorno il DTO aggiornato
        return response;
    }


    @Override
    public List<AttivitaSensibileDTO> getAllBySezione23(Long idSezione23) {

        if (idSezione23 == null) {
            throw new IllegalArgumentException("L'ID della Sezione23 non può essere nullo");
        }
        List<AttivitaSensibileDTO> response;
        try {
            // Recupero il riferimento alla Sezione23 senza eseguire subito la query
            Sezione23 sezione23 = sezione23Repository.getReferenceById(idSezione23);

            // Recupero tutte le Attività Sensibili associate alla Sezione23
            List<AttivitaSensibile> entities = attivitaSensibileRepository.getAttivitaSensibileBySezione23(sezione23);

            // Mapping Entity → DTO con eventuale arricchimento dei dati Mongo
            response = entities.stream()
                .map(entity -> {
                    // Mapping base JPA → DTO
                    AttivitaSensibileDTO attDTO = attivitaSensibileMapper.toDto(entity);

                    // Recupero UlterioriInfo da MongoDB (oggetto singolo)
                    attDTO.setUlterioriInfo(
                        Optional.ofNullable(
                                ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                                    entity.getId(),
                                    Sezione.SEZIONE_23_ATTIVITASENSIBILE
                                )
                            )
                            .map(info -> commonMapper.ulterioriInfoEntityToDto(info, new CycleAvoidingMappingContext()))
                            .orElse(null)
                    );

                    attDTO.setAttore(
                        Optional.ofNullable(
                                attoreRepository.findAllByExternalIdAndTipoSezione(
                                    entity.getId(),
                                    Sezione.SEZIONE_23_ATTIVITASENSIBILE
                                )
                            )
                            .map(a -> commonMapper.attoreEntityToDto(a ,new CycleAvoidingMappingContext()))
                            .orElse(null)
                    );


                    return attDTO;
                })
                .toList();

            // Pubblico evento di successo della transazione
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            // Logging dell'errore con riferimento all'id della sezione
            log.error(
                "Errore durante il recupero delle Attività Sensibili per Sezione23 id={}: {}",
                idSezione23,
                e.getMessage(),
                e
            );

            // Pubblico evento di fallimento della transazione
            eventPublisher.publishEvent(new TransactionFailureEvent<>(AttivitaSensibile.class, e));

            // Rilancio RuntimeException per gestione centralizzata degli errori
            throw new RuntimeException(
                "Errore durante il recupero delle Attività Sensibili per Sezione 23",
                e
            );
        }

        return response;
    }



    @Override
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

            // Cancellazione daa Postgres
            attivitaSensibileRepository.deleteById(id);

            // Propagazione della cancellazione su MongoDB
            attoreRepository.deleteByExternalId(id);
            ulterioriInfoRepository.deleteByExternalId(id);

            // evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("Attivita Sensibile con id={} cancellata con successo", id);

        } catch (Exception e) {
            // Log evento di fallimento
            log.error("Errore durante la cancellazione dell'Attivita Sensibile id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new AttivitaSensibile(), e));

            throw new RuntimeException("Errore durante la cancellazione dell'Attivita Sensibile", e);
        }
    }



}
