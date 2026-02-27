package it.ey.piao.api.service.impl;

import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.entity.Attivita;
import it.ey.entity.Attore;
import it.ey.entity.CategoriaObiettivi;
import it.ey.entity.Sezione4;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.CategoriaObiettiviMapper;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.ICategoriaObiettiviRepository;
import it.ey.piao.api.repository.ISezione4Repository;
import it.ey.piao.api.repository.ISottofaseMonitoraggioRepository;
import it.ey.piao.api.repository.mongo.IAttivitaRepository;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.ICategoriaObiettiviService;
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
public class CategoriaObiettiviServiceImpl implements ICategoriaObiettiviService {

    private final ICategoriaObiettiviRepository categoriaObiettiviRepository;
    private final CategoriaObiettiviMapper categoriaObiettiviMapper;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;
    private final IAttoreRepository attoreRepository;
    private final IAttivitaRepository attivitaRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ISezione4Repository sezione4Repository;
    private final ISottofaseMonitoraggioRepository sottofaseRepository;

    private static final Logger log = LoggerFactory.getLogger(CategoriaObiettiviServiceImpl.class);

    public CategoriaObiettiviServiceImpl(ICategoriaObiettiviRepository categoriaObiettiviRepository,
                                         CategoriaObiettiviMapper categoriaObiettiviMapper,
                                         CommonMapper commonMapper,
                                         MongoUtils mongoUtils,
                                         IAttoreRepository attoreRepository,
                                         IAttivitaRepository attivitaRepository,
                                         IUlterioriInfoRepository ulterioriInfoRepository,
                                         ApplicationEventPublisher eventPublisher,
                                         ISezione4Repository sezione4Repository,
                                         ISottofaseMonitoraggioRepository sottofaseRepository) {
        this.categoriaObiettiviRepository = categoriaObiettiviRepository;
        this.categoriaObiettiviMapper = categoriaObiettiviMapper;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
        this.attoreRepository = attoreRepository;
        this.attivitaRepository = attivitaRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.eventPublisher = eventPublisher;
        this.sezione4Repository = sezione4Repository;
        this.sottofaseRepository = sottofaseRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public CategoriaObiettiviDTO saveOrUpdate(CategoriaObiettiviDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        log.debug("DTO ricevuto nel service: {}", dto);
        log.debug("idSezione4 = {}", dto.getIdSezione4());

        CategoriaObiettiviDTO response;
        try {
            // DTO → Entity JPA
            CategoriaObiettivi entity = categoriaObiettiviMapper.toEntity(dto, context);

            // Relazione con Sezione4
            if (dto.getIdSezione4() == null) {
                throw new IllegalArgumentException("idSezione4 non può essere null");
            }
            Sezione4 s4 = sezione4Repository.getReferenceById(dto.getIdSezione4());
            entity.setSezione4(s4);

            // Relazione con SottofaseMonitoraggio (opzionale)
            if (dto.getIdSottofase() != null) {
                SottofaseMonitoraggio sottofase = sottofaseRepository.getReferenceById(dto.getIdSottofase());
                entity.setSottofase(sottofase);
            }

            // Salvo lo stato dell'entity per eventuale rollback
            if (entity.getId() != null) {
                categoriaObiettiviRepository.findById(entity.getId())
                    .ifPresent(existing ->
                        eventPublisher.publishEvent(new BeforeUpdateEvent<>(CategoriaObiettivi.class, existing))
                    );
            }

            // Salvo l'entity principale nel DB relazionale
            CategoriaObiettivi savedEntity = categoriaObiettiviRepository.save(entity);

            // Mappo l'entity salvata in DTO di risposta
            response = categoriaObiettiviMapper.toDto(savedEntity, context);

            // Salva i dati MongoDB (attore, attivita)
            saveMongoData(response, dto);

            // Pubblico evento successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante Save o update per CategoriaObiettivi id={}: {}", dto.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(categoriaObiettiviMapper.toEntity(dto, context), e));
            throw new RuntimeException("Errore durante il save o update della CategoriaObiettivi", e);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaObiettiviDTO> getAllBySezione4(Long idSezione4) {
        if (idSezione4 == null) {
            throw new IllegalArgumentException("L'ID della Sezione4 non può essere nullo");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        List<CategoriaObiettiviDTO> response;
        try {
            Sezione4 sezione4 = sezione4Repository.getReferenceById(idSezione4);

            List<CategoriaObiettivi> entities = categoriaObiettiviRepository.getCategoriaObiettiviBySezione4(sezione4);

            response = entities.stream()
                .map(entity -> {
                    CategoriaObiettiviDTO dto = categoriaObiettiviMapper.toDto(entity, context);
                    dto = loadMongoData(dto);
                    return dto;
                })
                .toList();

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante il recupero delle CategoriaObiettivi per Sezione4 id={}: {}",
                idSezione4, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(CategoriaObiettivi.class, e));
            throw new RuntimeException("Errore durante il recupero delle CategoriaObiettivi per Sezione4", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID della CategoriaObiettivi non può essere nullo");
        }

        try {
            Optional<CategoriaObiettivi> existing = categoriaObiettiviRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare una CategoriaObiettivi non esistente con id={}", id);
                throw new RuntimeException("CategoriaObiettivi non trovata con id: " + id);
            }

            eventPublisher.publishEvent(new BeforeUpdateEvent<>(CategoriaObiettivi.class, existing.get()));

            // Cancellazione da Postgres
            categoriaObiettiviRepository.deleteById(id);

            // Propagazione della cancellazione su MongoDB (ulterioriInfo, attore e attivita)
            ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);
            attoreRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);
            attivitaRepository.deleteByExternalId(id);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("CategoriaObiettivi con id={} cancellata con successo", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione della CategoriaObiettivi id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new CategoriaObiettivi(), e));
            throw new RuntimeException("Errore durante la cancellazione della CategoriaObiettivi", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaObiettiviDTO loadMongoData(CategoriaObiettiviDTO dto) {
        if (dto == null || dto.getId() == null) {
            log.warn("CategoriaObiettiviDTO o id è null, skip caricamento MongoDB");
            return dto;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupero UlterioriInfo da MongoDB
            dto.setUlterioriInfo(
                Optional.ofNullable(
                        ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                            dto.getId(),
                            Sezione.SEZIONE_4_CATEGORIAOBIETTIVI
                        )
                    )
                    .map(ultInfo -> commonMapper.ulterioriInfoEntityToDto(ultInfo, context))
                    .orElse(null)
            );

            // Recupero Attore da MongoDB
            dto.setAttore(
                Optional.ofNullable(
                        attoreRepository.findAllByExternalIdAndTipoSezione(
                            dto.getId(),
                            Sezione.SEZIONE_4_CATEGORIAOBIETTIVI
                        )
                    )
                    .map(attore -> commonMapper.attoreEntityToDto(attore, context))
                    .orElse(null)
            );

            // Recupero Attivita da MongoDB
            dto.setAttivita(
                Optional.ofNullable(
                        attivitaRepository.getByExternalId(dto.getId())
                    )
                    .map(attivita -> commonMapper.attivitaEntityToDto(attivita, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB caricati per CategoriaObiettivi id={}", dto.getId());
            return dto;

        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per CategoriaObiettivi id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB CategoriaObiettivi", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoData(CategoriaObiettiviDTO response, CategoriaObiettiviDTO request) {
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

                UlterioriInfo savedUlterioriInfo = mongoUtils.saveItem(
                    entityMongo,
                    ulterioriInfoRepository,
                    UlterioriInfo.class,
                    en -> en.setTipoSezione(Sezione.SEZIONE_4_CATEGORIAOBIETTIVI)
                );

                response.setUlterioriInfo(commonMapper.ulterioriInfoEntityToDto(savedUlterioriInfo, context));
            }

            // Salva Attore in MongoDB
            if (request.getAttore() != null) {
                Attore entityMongo = commonMapper.attoreDtoToEntity(request.getAttore(), context);
                entityMongo.setExternalId(response.getId());
                entityMongo.setTipoSezione(Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);

                Attore savedAttore = mongoUtils.saveItem(
                    entityMongo,
                    attoreRepository,
                    Attore.class,
                    en -> en.setTipoSezione(Sezione.SEZIONE_4_CATEGORIAOBIETTIVI)
                );

                response.setAttore(commonMapper.attoreEntityToDto(savedAttore, context));
            }

            // Salva Attivita in MongoDB
            if (request.getAttivita() != null) {
                Attivita entityMongo = commonMapper.attivitaDtoToEntity(request.getAttivita(), context);
                entityMongo.setExternalId(response.getId());

                Attivita savedAttivita = mongoUtils.saveItem(
                    entityMongo,
                    attivitaRepository,
                    Attivita.class,
                    en -> {}
                );

                response.setAttivita(commonMapper.attivitaEntityToDto(savedAttivita, context));
            }

            log.debug("Dati MongoDB salvati per CategoriaObiettivi id={}", response.getId());

        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per CategoriaObiettivi id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB CategoriaObiettivi", e);
        }
    }
}
