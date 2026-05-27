package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.dto.CategoriaObiettiviTipDTO;
import it.ey.entity.*;
import it.ey.enums.CodTipologiaCategoria;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CategoriaObiettiviMapper;
import it.ey.piao.api.mapper.CategoriaObiettiviTipMapper;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.IAttivitaRepository;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.ICategoriaObiettiviService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ICategoriaObiettiviTipRepository categoriaObiettiviTipRepository;
    private final CategoriaObiettiviTipMapper categoriaObiettiviTipMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

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
                                         ISottofaseMonitoraggioRepository sottofaseRepository,
                                         ICategoriaObiettiviTipRepository categoriaObiettiviTipRepository,
                                         CategoriaObiettiviTipMapper categoriaObiettiviTipMapper,
                                         StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
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
        this.categoriaObiettiviTipRepository = categoriaObiettiviTipRepository;
        this.categoriaObiettiviTipMapper = categoriaObiettiviTipMapper;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
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

            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(dto, dto.getIdSezione4(), dto.getIdPiao(), Sezione.SEZIONE_4);
            }
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getSezione4().getId(),Sezione.SEZIONE_4.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getSezione4().getId())
                        .codTipologiaFK(Sezione.SEZIONE_4.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }
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

            List<CategoriaObiettivi> entities = categoriaObiettiviRepository.getCategoriaObiettiviByIdSezione4(sezione4.getId());

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
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
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

            Optional<CategoriaObiettivi> categoriaObiettivi = categoriaObiettiviRepository.findById(id);

            // Cancellazione da Postgres
            categoriaObiettiviRepository.softDeleteById(id, LocalDateTime.now());

            // Propagazione della cancellazione su MongoDB (ulterioriInfo, attore e attivita)
            ulterioriInfoRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);
            attoreRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);
            attivitaRepository.deleteByExternalId(id);


            // Salva storico modifica dopo la cancellazione
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null) {
                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();
                storicoModificaHelper.salvaStoricoSePresente(dto, categoriaObiettivi.get().getSezione4().getId(), idPiao, Sezione.SEZIONE_4);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(categoriaObiettivi.get().getSezione4().getId(), Sezione.SEZIONE_4.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(categoriaObiettivi.get().getSezione4().getId())
                            .codTipologiaFK(Sezione.SEZIONE_4.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

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
            Attore attoreEntity = attoreRepository.findAllByExternalIdAndTipoSezione(
                dto.getId(),
                Sezione.SEZIONE_4_CATEGORIAOBIETTIVI
            );
            log.debug("Attore recuperato da MongoDB - externalIdFK: {}", attoreEntity != null ? attoreEntity.getExternalIdFK() : "entity null");

            dto.setAttore(
                Optional.ofNullable(attoreEntity)
                    .map(attore -> {
                        var attoreDto = commonMapper.attoreEntityToDto(attore, context);
                        log.debug("Attore DTO dopo mapping - externalIdFK: {}", attoreDto != null ? attoreDto.getExternalIdFK() : "dto null");
                        return attoreDto;
                    })
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
            UlterioriInfo ultInfoEntity = request.getUlterioriInfo() != null
                ? commonMapper.ulterioriInfoDtoToEntity(request.getUlterioriInfo(), context)
                : null;
            if (ultInfoEntity != null) ultInfoEntity.setExternalId(response.getId());
            UlterioriInfo savedUlterioriInfo = mongoUtils.saveItem(ultInfoEntity, response.getId(),
                ulterioriInfoRepository, UlterioriInfo.class,
                en -> en.setTipoSezione(Sezione.SEZIONE_4_CATEGORIAOBIETTIVI),
                "tipoSezione", Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);
            response.setUlterioriInfo(savedUlterioriInfo != null
                ? commonMapper.ulterioriInfoEntityToDto(savedUlterioriInfo, context)
                : null);

            // Salva Attore in MongoDB
            Attore attoreEntity = request.getAttore() != null
                ? commonMapper.attoreDtoToEntity(request.getAttore(), context)
                : null;
            if (attoreEntity != null) {
                log.debug("Request attore externalIdFK dal FE: {}", request.getAttore().getExternalIdFK());
                log.debug("Dopo mapping, entityMongo.externalIdFK: {}", attoreEntity.getExternalIdFK());

                attoreEntity.setExternalId(response.getId());
                attoreEntity.setTipoSezione(Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);

                // Preserva externalIdFK se passato dal FE
                if (request.getAttore().getExternalIdFK() != null) {
                    attoreEntity.setExternalIdFK(request.getAttore().getExternalIdFK());
                    log.debug("Impostato externalIdFK manualmente: {}", attoreEntity.getExternalIdFK());
                }

                // Se esiste già un documento con lo stesso externalId e tipoSezione, recupera l'id per aggiornarlo
                Attore existingAttore = attoreRepository.findAllByExternalIdAndTipoSezione(response.getId(), Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);
                if (existingAttore != null) {
                    attoreEntity.setId(existingAttore.getId());
                }
            }
            Attore savedAttore = mongoUtils.saveItem(attoreEntity, response.getId(),
                attoreRepository, Attore.class,
                en -> en.setTipoSezione(Sezione.SEZIONE_4_CATEGORIAOBIETTIVI),
                "tipoSezione", Sezione.SEZIONE_4_CATEGORIAOBIETTIVI);

            log.debug("Attore salvato con externalIdFK: {}", savedAttore != null ? savedAttore.getExternalIdFK() : "null");
            response.setAttore(savedAttore != null
                ? commonMapper.attoreEntityToDto(savedAttore, context)
                : null);
            log.debug("Attore DTO risposta externalIdFK: {}", response.getAttore() != null ? response.getAttore().getExternalIdFK() : "null");

            // Salva Attivita in MongoDB
            if (request.getAttivita() != null) {
                Attivita entityMongo = commonMapper.attivitaDtoToEntity(request.getAttivita(), context);
                entityMongo.setExternalId(response.getId());

                // Se esiste già un documento con lo stesso externalId, recupera l'id per aggiornarlo
                Attivita existing = attivitaRepository.getByExternalId(response.getId());
                if (existing != null) {
                    entityMongo.setId(existing.getId());
                }

                Attivita savedAttivita = attivitaRepository.save(entityMongo);

                response.setAttivita(commonMapper.attivitaEntityToDto(savedAttivita, context));
            }

            log.debug("Dati MongoDB salvati per CategoriaObiettivi id={}", response.getId());

        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per CategoriaObiettivi id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB CategoriaObiettivi", e);
        }
    }

    @Override
    public List<CategoriaObiettiviTipDTO> getAllCategoriaObiettiviTipPerCodTipologiaFK(CodTipologiaCategoria codTipologiaFK) {
        if(codTipologiaFK == null)
        {
            throw new IllegalArgumentException("Il codTipologiaFK passato non e' valido");
        }

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            return categoriaObiettiviTipRepository.getAllCategoriaObiettiviTipPerCodTipologiaFK(codTipologiaFK)
                .stream()
                .map(entity -> categoriaObiettiviTipMapper.toDto(entity, context))
                .toList();

        } catch (Exception e) {
            log.error("Errore durante il recupero delle CategoriaObiettiviTip: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle CategoriaObiettiviTip", e);
        }
    }
}
