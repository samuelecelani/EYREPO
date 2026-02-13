package it.ey.piao.api.service.impl;

import it.ey.dto.EventoRischioDTO;
import it.ey.entity.AttivitaSensibile;
import it.ey.entity.EventoRischio;
import it.ey.entity.Fattore;
import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.EventoRischioMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAttivitaSensibileRepository;
import it.ey.piao.api.repository.IEventoRischioRepository;
import it.ey.piao.api.repository.mongo.IFattoreRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IEventoRischioService;
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

import java.util.List;
import java.util.Optional;

@Service
public class EventoRischioServiceImpl implements IEventoRischioService {

    private final IEventoRischioRepository eventoRischioRepository;
    private final IAttivitaSensibileRepository attivitaSensibileRepository;
    private final EventoRischioMapper eventoRischioMapper;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final IFattoreRepository fattoreRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final Logger log = LoggerFactory.getLogger(EventoRischioServiceImpl.class);

    public EventoRischioServiceImpl(IEventoRischioRepository eventoRischioRepository,
                                    IAttivitaSensibileRepository attivitaSensibileRepository,
                                    EventoRischioMapper eventoRischioMapper,
                                    CommonMapper commonMapper,
                                    MongoUtils mongoUtils,
                                    IUlterioriInfoRepository ulterioriInfoRepository,
                                    IFattoreRepository fattoreRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.eventoRischioRepository = eventoRischioRepository;
        this.attivitaSensibileRepository = attivitaSensibileRepository;
        this.eventoRischioMapper = eventoRischioMapper;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.fattoreRepository = fattoreRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public EventoRischioDTO saveOrUpdate(EventoRischioDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        EventoRischioDTO response;
        try {
            // DTO in entity JPA
            EventoRischio entity = eventoRischioMapper.toEntity(dto,context);

            // Relazione con AttivitaSensibile
            if (dto.getAttivitaSensibile() != null && dto.getAttivitaSensibile().getId() != null) {
                entity.setAttivitaSensibile(attivitaSensibileRepository.getReferenceById(dto.getAttivitaSensibile().getId()));
            }

            // Salvo lo stato dell'entity per eventuale rollback
            if (entity.getId() != null) {
                eventoRischioRepository.findById(entity.getId())
                    .ifPresent(existing ->
                        eventPublisher.publishEvent(new BeforeUpdateEvent<>(EventoRischio.class, existing))
                    );
            }

            // Salvo l'entity principale nel DB relazionale
            EventoRischio savedEntity = eventoRischioRepository.save(entity);

            // Mappo l'entity salvata in DTO di risposta
            response = eventoRischioMapper.toDto(savedEntity,context);

            // Salva i dati MongoDB tramite metodo dedicato
            saveMongoData(response, dto);

            // Pubblico evento successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante Save o update per EventoRischio id={}: {}", dto.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(eventoRischioMapper.toEntity(dto,context), e));
            throw new RuntimeException("Errore durante il save o update dell'EventoRischio", e);
        }

        return response;
    }

    @Override
     @Transactional(readOnly = true)
    public List<EventoRischioDTO> getAllByAttivitaSensibile(Long idAttivitaSensibile) {
        if (idAttivitaSensibile == null) {
            throw new IllegalArgumentException("L'ID dell'AttivitaSensibile non può essere nullo");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        List<EventoRischioDTO> response;
        try {
            // Recupero il riferimento all'AttivitaSensibile senza eseguire subito la query
            AttivitaSensibile attivitaSensibile = attivitaSensibileRepository.getReferenceById(idAttivitaSensibile);

            // Recupero tutti gli EventiRischio associati all'AttivitaSensibile
            List<EventoRischio> entities = eventoRischioRepository.findByAttivitaSensibile(attivitaSensibile);

            // Mapping Entity → DTO con arricchimento dei dati Mongo
            response = entities.stream()
                .map(entity -> {
                    // Mapping base JPA → DTO
                    EventoRischioDTO eventoDTO = eventoRischioMapper.toDto(entity,context);

                    // Carica i dati MongoDB
                    eventoDTO = loadMongoData(eventoDTO);

                    return eventoDTO;
                })
                .toList();

            // Pubblico evento di successo della transazione
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante il recupero degli EventiRischio per AttivitaSensibile id={}: {}",
                idAttivitaSensibile, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(EventoRischio.class, e));
            throw new RuntimeException("Errore durante il recupero degli EventiRischio per AttivitaSensibile", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID dell'EventoRischio non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<EventoRischio> existing = eventoRischioRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un EventoRischio non esistente con id={}", id);
                throw new RuntimeException("EventoRischio non trovato con id: " + id);
            }

            // Evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(EventoRischio.class, existing.get()));

            // Cancellazione da Postgres
            eventoRischioRepository.deleteById(id);

            // Propagazione della cancellazione su MongoDB
            fattoreRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_23_EVENTORISCHIO);
            ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_23_EVENTORISCHIO);

            // Evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("EventoRischio con id={} cancellato con successo", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione dell'EventoRischio id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new EventoRischio(), e));
            throw new RuntimeException("Errore durante la cancellazione dell'EventoRischio", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventoRischioDTO loadMongoData(EventoRischioDTO eventoRischio) {
        if (eventoRischio == null || eventoRischio.getId() == null) {
            log.warn("EventoRischioDTO o id è null, skip caricamento MongoDB");
            return eventoRischio;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupero UlterioriInfo da MongoDB
            eventoRischio.setUlterioriInfo(
                Optional.ofNullable(
                        ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                            eventoRischio.getId(),
                            Sezione.SEZIONE_23_EVENTORISCHIO
                        )
                    )
                    .map(info -> commonMapper.ulterioriInfoEntityToDto(info, context))
                    .orElse(null)
            );

            // Recupero Fattore da MongoDB
            eventoRischio.setFattore(
                Optional.ofNullable(
                        fattoreRepository.findByExternalIdAndTipoSezione(
                            eventoRischio.getId(),
                            Sezione.SEZIONE_23_EVENTORISCHIO
                        )
                    // *********************************************************Chiedere a Gianni
                    )
                    .map(fattore -> eventoRischioMapper.fattoreToDto(fattore,context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB caricati per EventoRischio id={}", eventoRischio.getId());
            return eventoRischio;

        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per EventoRischio id={}: {}", eventoRischio.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB EventoRischio", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoData(EventoRischioDTO response, EventoRischioDTO request) {
        if (response == null || response.getId() == null || request == null) {
            log.warn("Response, Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo in MongoDB
            if (request.getUlterioriInfo() != null) {
                UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(request.getUlterioriInfo(), context);
                entityMongo.setExternalId(response.getId());

                UlterioriInfo savedInfo = mongoUtils.saveItem(
                    entityMongo,
                    ulterioriInfoRepository,
                    UlterioriInfo.class,
                    en -> en.setTipoSezione(Sezione.SEZIONE_23_EVENTORISCHIO)
                );

                response.setUlterioriInfo(commonMapper.ulterioriInfoEntityToDto(savedInfo, context));
            }

            // Salva Fattore in MongoDB
            if (request.getFattore() != null) {
                Fattore entityMongo = eventoRischioMapper.fattoreToEntity(request.getFattore(),context);
                entityMongo.setExternalId(response.getId());
                entityMongo.setTipoSezione(Sezione.SEZIONE_23_EVENTORISCHIO);

                Fattore savedFattore = mongoUtils.saveAllItems(entityMongo, fattoreRepository, Fattore.class);

                //+++++++++++++++++++++++++++++++++ Chiedere a Gianni
                response.setFattore(eventoRischioMapper.fattoreToDto(savedFattore,context));
            }

            log.debug("Dati MongoDB salvati per EventoRischio id={}", response.getId());

        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per EventoRischio id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB EventoRischio", e);
        }
    }
}
