package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.enums.TypeErrorEnum;
import it.ey.piao.api.exception.BusinessException;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.ObbiettivoPerformanceMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.IContributoreInternoRepository;
import it.ey.piao.api.service.IIndicatoreService;
import it.ey.piao.api.service.IObbiettivoPerformanceService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ObbiettivoPerformanceServiceImpl implements IObbiettivoPerformanceService {

    private final IObbiettivoPerformanceRepository obbiettivoPerformanceRepository;
    private final ISezione22Repository sezione22Repository;
    private final OVPRepository ovpRepository;
    private final IOVPStrategiaRepository ovpStrategiaRepository;
    private final ObbiettivoPerformanceMapper obbiettivoPerformanceMapper;
    private final IContributoreInternoRepository contributoreInternoRepository;
    private final CommonMapper commonMapper;
    private final IStakeHolderRepository stakeHolderRepository;
    private final IIndicatoreRepository indicatoreRepository;
    private final MongoUtils mongoUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final IIndicatoreService indicatoreService;
    private final DeleteDependencyService deleteDependencyService;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final IObiettivoPerformanceStakeHolderRepository obiettivoPerformanceStakeHolderRepository;
    private final IObiettivoPerformanceIndicatoreRepository obiettivoPerformanceIndicatoreRepository;

    private static final Logger log = LoggerFactory.getLogger(ObbiettivoPerformanceServiceImpl.class);

    public ObbiettivoPerformanceServiceImpl(
        IObbiettivoPerformanceRepository obbiettivoPerformanceRepository,
        ISezione22Repository sezione22Repository,
        OVPRepository ovpRepository,
        IOVPStrategiaRepository ovpStrategiaRepository,
        ObbiettivoPerformanceMapper obbiettivoPerformanceMapper,
        IContributoreInternoRepository contributoreInternoRepository,
        CommonMapper commonMapper,
        IStakeHolderRepository stakeHolderRepository,
        IIndicatoreRepository indicatoreRepository,
        MongoUtils mongoUtil,
        ApplicationEventPublisher eventPublisher,
        IIndicatoreService indicatoreService,
        DeleteDependencyService deleteDependencyService,
        StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, IObiettivoPerformanceStakeHolderRepository obiettivoPerformanceStakeHolderRepository, IObiettivoPerformanceIndicatoreRepository obiettivoPerformanceIndicatoreRepository) {
        this.obbiettivoPerformanceRepository = obbiettivoPerformanceRepository;
        this.sezione22Repository = sezione22Repository;
        this.ovpRepository = ovpRepository;
        this.ovpStrategiaRepository = ovpStrategiaRepository;
        this.obbiettivoPerformanceMapper = obbiettivoPerformanceMapper;
        this.contributoreInternoRepository = contributoreInternoRepository;
        this.commonMapper = commonMapper;
        this.stakeHolderRepository = stakeHolderRepository;
        this.indicatoreRepository = indicatoreRepository;
        this.mongoUtil = mongoUtil;
        this.eventPublisher = eventPublisher;
        this.indicatoreService = indicatoreService;
        this.deleteDependencyService = deleteDependencyService;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.obiettivoPerformanceStakeHolderRepository = obiettivoPerformanceStakeHolderRepository;
        this.obiettivoPerformanceIndicatoreRepository = obiettivoPerformanceIndicatoreRepository;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public ObbiettivoPerformanceDTO saveOrUpdate(ObbiettivoPerformanceDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        ObbiettivoPerformanceDTO response;

        try {
            // Mapping del DTO all'entità
            ObbiettivoPerformance entity = obbiettivoPerformanceMapper.toEntity(request,new CycleAvoidingMappingContext());
            // Salvo lo stato dell'oggetto per un eventuale rollback
            if (entity.getId() != null) {
                obbiettivoPerformanceRepository.findById(entity.getId()).ifPresent(existing -> {
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(ObbiettivoPerformance.class, existing));
                });
            }
            // Impostazione delle relazioni usando getReferenceById
            entity.setSezione22(sezione22Repository.getReferenceById(request.getIdSezione22()));
            if(request.getIdOvp() != null){
                entity.setOvp(ovpRepository.findById(request.getIdOvp()).orElseThrow());

            }
            if(request.getIdStrategiaOvp() != null){
                entity.setOvpStrategia(ovpStrategiaRepository.getReferenceById(request.getIdStrategiaOvp()));
            }

            // Sincronizzazione StakeHolders (OneToMany)
            syncStakeHolders(entity, request.getStakeholders());

            // Sincronizzazione Indicatori (OneToMany)
            syncIndicatori(entity, request.getIndicatori());

            // Salvataggio dell'entità principale (Hibernate gestisce insert/update automaticamente)
            ObbiettivoPerformance savedEntity = obbiettivoPerformanceRepository.save(entity);

            // Mapping del risultato
            response = obbiettivoPerformanceMapper.toDto( savedEntity,new CycleAvoidingMappingContext());


            // Gestione Contributo Interno (MongoDB)
            ContributoreInterno contributoreEntity = request.getContributoreInterno() != null
                ? commonMapper.contributoreInternoDtoToEntity(request.getContributoreInterno(), new CycleAvoidingMappingContext())
                : null;
            if (contributoreEntity != null) contributoreEntity.setExternalId(savedEntity.getId());
            ContributoreInterno savedContrib = mongoUtil.saveItem(contributoreEntity, savedEntity.getId(),
                contributoreInternoRepository, ContributoreInterno.class);
            response.setContributoreInterno(savedContrib != null
                ? commonMapper.contributoreInternoEntityToDto(savedContrib, new CycleAvoidingMappingContext())
                : null);

            if (request.getCampiModificati() != null && !request.getCampiModificati().isBlank() && request.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(request, request.getIdSezione22(), request.getIdPiao(), Sezione.SEZIONE_22);
            }
            if (request.getStatoSezione() != null && !request.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(request.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getSezione22().getId(),Sezione.SEZIONE_22.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(request.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getSezione22().getId())
                        .codTipologiaFK(Sezione.SEZIONE_22.name())
                        .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build());
            }
            // Pubblicazione evento di successo dopo il commit
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate ObbiettivoPerformance per id={}: {}",
                request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(obbiettivoPerformanceMapper.toEntity(request,new CycleAvoidingMappingContext()), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento dell'ObbiettivoPerformance", e);
        }

        return response;
    }

    @Override
    @Transactional()
    public List<ObbiettivoPerformanceDTO> saveOrUpdateAll(List<ObbiettivoPerformanceDTO> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("La lista di richieste non può essere nulla o vuota");
        }

        List<ObbiettivoPerformanceDTO> responses = new ArrayList<>();

        try {
          List<ObbiettivoPerformance>  entities = obbiettivoPerformanceMapper.toEntityList(requests, new CycleAvoidingMappingContext());

            if (entities != null && !entities.isEmpty()) {

                // prendi solo quelli con id
                List<ObbiettivoPerformance> existingList = entities.stream()
                    .filter(e -> e.getId() != null)
                    .map(e -> obbiettivoPerformanceRepository.findById(e.getId()).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

                if (!existingList.isEmpty()) {
                    eventPublisher.publishEvent(
                        new BeforeUpdateEvent<>(ObbiettivoPerformance.class, existingList)
                    );
                }

            }

            List<ObbiettivoPerformance> entitiesToSave = new ArrayList<>();

            // Fase 1: Preparazione entità (mapping e relazioni)
            for (ObbiettivoPerformanceDTO request : requests) {
                if (request == null) {
                    log.warn("Elemento null trovato nella lista, verrà saltato");
                    continue;
                }

                // Mapping del DTO all'entità
                ObbiettivoPerformance entity = obbiettivoPerformanceMapper.toEntity(request,new CycleAvoidingMappingContext());

                // Impostazione delle relazioni usando getReferenceById (lazy proxy - no query)
                entity.setSezione22(sezione22Repository.getReferenceById(request.getIdSezione22()));

                //TODO CAPIRE LATO FUNZIONALE
                if(request.getIdOvp() != null){
                    entity.setOvp(ovpRepository.getReferenceById(request.getIdOvp()));
                }

                if(request.getIdStrategiaOvp() != null){
                    entity.setOvpStrategia(ovpStrategiaRepository.getReferenceById(request.getIdStrategiaOvp()));
                }

                // Sincronizzazione StakeHolders e Indicatori (OneToMany)
                syncStakeHolders(entity, request.getStakeholders());
                syncIndicatori(entity, request.getIndicatori());

                entitiesToSave.add(entity);
            }

            // Fase 2: Salvataggio batch di tutte le entità (una transazione, batch insert/update)
            List<ObbiettivoPerformance> savedEntities = obbiettivoPerformanceRepository.saveAll(entitiesToSave);

            // ============= OTTIMIZZAZIONE 2: Batch saving su MongoDB =============
            // Preparo tutti i ContributoriInterni da salvare in batch
            List<ContributoreInterno> contributoriToSave = new ArrayList<>();
            for (int i = 0; i < savedEntities.size(); i++) {
                ObbiettivoPerformance savedEntity = savedEntities.get(i);
                ObbiettivoPerformanceDTO request = requests.get(i);

                if (request.getContributoreInterno() != null) {
                    ContributoreInterno entityMongo =commonMapper.contributoreInternoDtoToEntity(request.getContributoreInterno(),new CycleAvoidingMappingContext());
                    entityMongo.setExternalId(savedEntity.getId());
                    contributoriToSave.add(entityMongo);
                }
            }

            // Salvataggio batch su MongoDB (una sola operazione invece di N)
            List<ContributoreInterno> savedContributori = contributoriToSave.isEmpty()
                ? new ArrayList<>()
                : contributoreInternoRepository.saveAll(contributoriToSave);

            // Creo una mappa per lookup veloce O(1) invece di O(N)
            var contributoriMap = savedContributori.stream()
                .collect(java.util.stream.Collectors.toMap(
                    ContributoreInterno::getExternalId,
                    c -> c,
                    (existing, replacement) -> replacement
                ));

            // Fase 3: Mapping delle risposte (riduzione mapping ripetitivi)
            for (ObbiettivoPerformance savedEntity : savedEntities) {
                // Mapping del risultato
                ObbiettivoPerformanceDTO response = obbiettivoPerformanceMapper.toDto(savedEntity,new CycleAvoidingMappingContext());


                // Recupero Contributore Interno dalla mappa (O(1) lookup)
                response.setContributoreInterno(
                    Optional.ofNullable(contributoriMap.get(savedEntity.getId()))
                        .map(c ->commonMapper.contributoreInternoEntityToDto(c,new CycleAvoidingMappingContext()))
                        .orElse(null)
                );

                responses.add(response);

                // Pubblicazione evento di successo per ogni elemento
                eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
            }

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdateAll di {} ObiettiviPerformance: {}",
                requests.size(), e.getMessage(), e);

            // Pubblicazione eventi di fallimento (stream per efficienza)
            requests.stream()
                .filter(Objects::nonNull)
                .forEach(request -> eventPublisher.publishEvent(
                    new TransactionFailureEvent<>(obbiettivoPerformanceMapper.toEntity(request,new CycleAvoidingMappingContext()), e)
                ));

            throw new RuntimeException("Errore durante il salvataggio o aggiornamento batch degli ObiettiviPerformance", e);
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObbiettivoPerformanceDTO> getAllBySezione22(Long idSezione22) {
        if (idSezione22 == null) {
            throw new IllegalArgumentException("L'ID della Sezione22 non può essere nullo");
        }

        try {
            List<ObbiettivoPerformance> entities = obbiettivoPerformanceRepository.findByIdSezione22(idSezione22);

            return entities.stream()
                    .map(entity -> {
                        ObbiettivoPerformanceDTO dto =obbiettivoPerformanceMapper.toDto(entity,new CycleAvoidingMappingContext());

                        // Carica i dati MongoDB
                        loadMongoDataForObiettivo(dto);

                        return dto;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Errore durante il recupero degli ObiettiviPerformance per Sezione22 id={}: {}",
                    idSezione22, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli ObiettiviPerformance", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 10)
    public void deleteById(Long id,
                           String campiModificati, Long idPiao, String testoSezione,
                           String updatedByNameSurname, String updatedByRole,
                           boolean forceDelete, String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("L'ID non può essere nullo");
        }

        try {
            log.info("Eliminazione ObbiettivoPerformance con ID={} (forceDelete={})", id, forceDelete);

            ObbiettivoPerformance entity =
                obbiettivoPerformanceRepository.findById(id)
                    .orElseThrow(() ->
                        new BusinessException(
                            BusinessException.INTERNAL_ERROR,
                            "ObbiettivoPerformance non trovato con id: " + id,
                            TypeErrorEnum.ERROR
                        )
                    );

            // mappo DTO per la validazione
            ObbiettivoPerformanceDTO dto = obbiettivoPerformanceMapper.toDto(entity, new CycleAvoidingMappingContext());

            // PRIMO GIRO WARNING
                deleteDependencyService.validateBeforeDeleteOrUpdate(id, dto, forceDelete);

                if (dto.getTypeEnum() == TypeErrorEnum.ERROR) {
                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.ERROR);
                }

                if (dto.getTypeEnum() == TypeErrorEnum.WARNING) {
                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.WARNING);
                }

                dto.setTypeEnum(null);
                dto.setErrorCode(null);
                dto.setMessageError(null);

                Optional.ofNullable(contributoreInternoRepository.getByExternalId(id))
                    .ifPresent(contributoreInternoRepository::delete);


            LocalDateTime deactivationTime = LocalDateTime.now();

            // SOFT DELETE tabella relazionale con stakeholder
            obiettivoPerformanceStakeHolderRepository.softDeleteByObiettivoId(id,deactivationTime);

            // SOFT DELETE tabella relazionale con indicatore
            obiettivoPerformanceIndicatoreRepository.softDeleteByObiettivoId(id,deactivationTime);


            obbiettivoPerformanceRepository.softDeleteById(id, deactivationTime);


            //Da testare e capire se va aggiunto
            /*
            entity.setActive(false);
            entity.setDeactivationTime(deactivationTime);

             */



            // STORICO SE PRESENTE
                if (campiModificati != null &&
                    !campiModificati.isBlank() &&
                    idPiao != null &&
                    entity.getSezione22() != null &&
                    entity.getSezione22().getId() != null) {

                    BaseDTO dtoStorico = BaseDTO.builder()
                        .campiModificati(campiModificati)
                        .idPiao(idPiao)
                        .updatedByNameSurname(updatedByNameSurname)
                        .updatedByRole(updatedByRole)
                        .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                        .build();

                    storicoModificaHelper.salvaStoricoSePresente(
                        dtoStorico,
                        entity.getSezione22().getId(),
                        idPiao,
                        Sezione.SEZIONE_22
                    );
                }

                // Salva storico stato sezione dopo la cancellazione
                if (statoSezione != null && !statoSezione.isBlank() &&
                    entity.getSezione22() != null && entity.getSezione22().getId() != null) {
                    if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(entity.getSezione22().getId(), Sezione.SEZIONE_22.name())))) {
                        storicoStatoSezioneRepository.save(
                            StoricoStatoSezione.builder().statoSezione(
                                    StatoSezione.builder()
                                        .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                        .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                        .build())
                                .idEntitaFK(entity.getSezione22().getId())
                                .codTipologiaFK(Sezione.SEZIONE_22.name())
                                .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                .createdByNameSurname(updatedByNameSurname)
                                .createdByRole(updatedByRole)
                                .build());
                    }
                }

                log.info("ObbiettivoPerformance con id={} eliminato con successo", id);
                return;


        } catch (BusinessException ex) {
            throw ex;

        } catch (Exception e) {

            log.error("Errore durante la cancellazione dell'ObbiettivoPerformance id={}: {}",
                id, e.getMessage(), e);

            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante la cancellazione dell'ObbiettivoPerformance",
                TypeErrorEnum.ERROR
            );
        }
    }




    @Override
    @Transactional(readOnly = true)
    public List<ObbiettivoPerformanceDTO> findByTipologiaAndFilters(TipologiaObbiettivo tipologia, Long idOvp, Long idStrategia) {
        if (tipologia == null) {
            throw new IllegalArgumentException("La tipologia non può essere nulla");
        }

        try {
            log.debug("Recupero ObiettiviPerformance con tipologia={}, idOvp={}, idStrategia={}", tipologia, idOvp, idStrategia);
            List<ObbiettivoPerformance> entities = obbiettivoPerformanceRepository.findByTipologiaAndFilters(tipologia, idOvp, idStrategia);

            return entities.stream()
                .map(entity -> {
                    ObbiettivoPerformanceDTO dto = obbiettivoPerformanceMapper.toDto(entity, new CycleAvoidingMappingContext());


                    // Carica i dati MongoDB
                    loadMongoDataForObiettivo(dto);

                    return dto;
                })
                .toList();
        } catch (Exception e) {
            log.error("Errore durante il recupero degli ObiettiviPerformance con filtri tipologia={}, idOvp={}, idStrategia={}: {}",
                tipologia, idOvp, idStrategia, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli ObiettiviPerformance con filtri", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public ObbiettivoPerformanceDTO enrichWithRelations(ObbiettivoPerformance entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Il DTO e l'ID non possono essere nulli");
        }

        try {
            ObbiettivoPerformanceDTO dto =obbiettivoPerformanceMapper.toDto(entity,new CycleAvoidingMappingContext());

            // Carica i dati MongoDB
            loadMongoDataForObiettivo(dto);

            log.debug("ObbiettivoPerformanceDTO con id={} arricchito con relazioni", entity.getId());
            return dto;
        } catch (Exception e) {
            log.error("Errore durante l'arricchimento dell'ObbiettivoPerformanceDTO con id={}: {}",
                entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'arricchimento dell'ObbiettivoPerformanceDTO con le relazioni", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void loadMongoDataForObiettivo(ObbiettivoPerformanceDTO obiettivoDTO) {
        if (obiettivoDTO == null || obiettivoDTO.getId() == null) {
            log.warn("ObbiettivoPerformanceDTO o ID è null, skip caricamento MongoDB");
            return;
        }

        try {
            // Carica ContributoreInterno da MongoDB
            obiettivoDTO.setContributoreInterno(
                Optional.ofNullable(
                        contributoreInternoRepository.getByExternalId(obiettivoDTO.getId())
                    )
                    .map(u -> commonMapper.contributoreInternoEntityToDto(u,new CycleAvoidingMappingContext()))
                    .orElse(null)
            );

            // Carica i dati MongoDB per ogni Indicatore
            if (obiettivoDTO.getIndicatori() != null && !obiettivoDTO.getIndicatori().isEmpty()) {
                obiettivoDTO.getIndicatori().forEach(indDTO -> {
                    if (indDTO.getIndicatore() != null && indDTO.getIndicatore().getId() != null) {
                        // Usa enrichWithRelations per caricare tutti i dati MongoDB dell'indicatore
                        Indicatore indicatoreEntity = indicatoreRepository.getReferenceById(indDTO.getIndicatore().getId());
                        IndicatoreDTO indicatoreCompleto = indicatoreService.enrichWithRelations(indicatoreEntity);
                        indDTO.setIndicatore(indicatoreCompleto);
                    }
                });
            }

            log.debug("Dati MongoDB caricati per ObbiettivoPerformance id={}", obiettivoDTO.getId());
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per ObbiettivoPerformance id={}: {}",
                obiettivoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB ObbiettivoPerformance", e);
        }
    }
    /*** Sincronizza la lista di StakeHolders dell'obiettivo.
     * Gli stakeholder esistono già nel DB, va solo settato il riferimento all'obiettivo.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni.
     */
    private void syncStakeHolders(ObbiettivoPerformance parent, List<ObiettivoStakeHolderDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setStakeholders(new ArrayList<>());
            return;
        }

        List<ObiettivoPerformanceStakeHolder> entities = new ArrayList<>();

        for (ObiettivoStakeHolderDTO dto : dtoList) {
            ObiettivoPerformanceStakeHolder entity = ObiettivoPerformanceStakeHolder.builder()
                .id(dto.getId())
                .obbiettivoPerformance(parent)
                .stakeholder( stakeHolderRepository.getReferenceById(dto.getStakeholder().getId()))
                .build();
            entities.add(entity);
        }

        parent.setStakeholders(entities);
    }

    /**
     * Sincronizza la lista di Indicatori dell'obiettivo.
     * Gli indicatori esistono già nel DB, va solo settato il riferimento all'obiettivo.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni.
     */
    private void syncIndicatori(ObbiettivoPerformance parent, List<ObiettivoIndicatoriDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setIndicatori(new ArrayList<>());
            return;
        }

        List<ObiettivoPerformanceIndicatore> entities = new ArrayList<>();

        for (ObiettivoIndicatoriDTO dto : dtoList) {
            ObiettivoPerformanceIndicatore entity = ObiettivoPerformanceIndicatore.builder()
                .id(dto.getId())
                .obbiettivoPerformance(parent)
                .indicatore(indicatoreRepository.getReferenceById(dto.getIndicatore().getId()))
                .build();
            entities.add(entity);
        }

        parent.setIndicatori(entities);
    }
}
