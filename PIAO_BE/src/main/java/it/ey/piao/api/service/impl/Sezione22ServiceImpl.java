package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.dto.Sezione22DTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione22Mapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.ISezione22Repository;
import it.ey.piao.api.repository.mongo.*;
import it.ey.piao.api.service.IObbiettivoPerformanceService;
import it.ey.piao.api.service.IFaseService;
import it.ey.piao.api.service.IAdempimentoService;
import it.ey.piao.api.service.ISezione22Service;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class Sezione22ServiceImpl implements ISezione22Service {

    private final ISezione22Repository sezione22Repository ;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PiaoMapper piaoMapper; // MapStruct per entità comuni
    private final Sezione22Mapper sezione22Mapper; // MapStruct mapper
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtil;
    private final IAzioneRepository azioneRepository;
    private final IObbiettivoPerformanceService obbiettivoPerformanceService;
    private final IFaseService faseService;
    private final IAdempimentoService adempimentoService;
    private final AllegatoRepository allegatoRepository;

    private static final Logger log = LoggerFactory.getLogger(Sezione22ServiceImpl.class);

    public  Sezione22ServiceImpl(ISezione22Repository sezione22Repository, IUlterioriInfoRepository ulterioriInfoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, ApplicationEventPublisher eventPublisher, PiaoMapper piaoMapper, Sezione22Mapper sezione22Mapper, CommonMapper commonMapper, MongoUtils mongoUtil, IAzioneRepository azioneRepository, IObbiettivoPerformanceService obbiettivoPerformanceService, IFaseService faseService, IAdempimentoService adempimentoService, AllegatoRepository allegatoRepository) {
        this.sezione22Repository = sezione22Repository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.eventPublisher = eventPublisher;
        this.piaoMapper = piaoMapper;
        this.sezione22Mapper = sezione22Mapper;
        this.commonMapper = commonMapper;
        this.mongoUtil = mongoUtil;
        this.azioneRepository = azioneRepository;
        this.obbiettivoPerformanceService = obbiettivoPerformanceService;
        this.faseService = faseService;
        this.adempimentoService = adempimentoService;
        this.allegatoRepository = allegatoRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione22DTO getOrCreateSezione22(PiaoDTO piao) {

        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione22 dal repository usando il Piao
            Sezione22 existing = sezione22Repository.findByPiao(piaoMapper.toEntity(piao, context));

            if (existing != null) {
                // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
                Sezione22DTO response = sezione22Mapper.toDto(existing, context);

                //Recupero lo stato della singola sezione
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_22.name())));

                // Carica SOLO i dati MongoDB della sezione22
                response = loadMongoDataSezione22(response);

                // Carica i dati MongoDB per ogni Obiettivo Performance (indicatori con UlterioriInfo MongoDB)
                if (response.getObbiettiviPerformance() != null && !response.getObbiettiviPerformance().isEmpty()) {
                    response.getObbiettiviPerformance().forEach(obbiettivoPerformanceService::loadMongoDataForObiettivo);
                }

                log.info("Sezione22 trovata per PIAO id={} con {} obiettivi", piao.getId(),
                    response.getObbiettiviPerformance() != null ? response.getObbiettiviPerformance().size() : 0);
                return response;
            }

            // Creazione nuova Sezione22 se non esiste
            log.info("Nessuna Sezione22 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione22 nuova = Sezione22.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .build();

            Sezione22 salvata = sezione22Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_22.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .build());

            Sezione22DTO response = sezione22Mapper.toDto(salvata,context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione22 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione22", e);
        }
    }

    @Override
    @Transactional(readOnly = true,propagation = Propagation.REQUIRED)
    public Sezione22DTO loadMongoDataSezione22(Sezione22DTO sezione22) {
        if (sezione22 == null ) {
            log.warn("Sezione22DTO o idSezione22 è null, skip caricamento MongoDB");
            return sezione22;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Caricamento dati MongoDB
            sezione22.setUlterioriInfo(
                Optional.ofNullable(
                        ulterioriInfoRepository.findByExternalIdAndTipoSezione(sezione22.getId(), Sezione.SEZIONE_22)
                    )
                    .map(e -> commonMapper.ulterioriInfoEntityToDto(e, context))
                    .orElse(null)
            );

            // Carica dati MongoDB per Adempimenti
            if (sezione22.getAdempimenti() != null && !sezione22.getAdempimenti().isEmpty()) {
                sezione22.getAdempimenti().forEach(a -> {
                    // Azione (opzionale) - usa l'ID dell'adempimento, non della sezione
                    AzioneDTO azione = Optional.ofNullable(
                            azioneRepository.getByExternalId(a.getId())
                        )
                        .map(e -> commonMapper.azioneEntityToDto(e, context))
                        .orElse(null);
                    a.setAzione(azione);

                    // Ulteriori info (opzionali) - usa l'ID dell'adempimento, non della sezione
                    UlterioriInfoDTO ulterioriInfo = Optional.ofNullable(
                            ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                                a.getId(), Sezione.SEZIONE_22_ADEMPIMENTO
                            )
                        )
                        .map(e -> commonMapper.ulterioriInfoEntityToDto(e, context))
                        .orElse(null);
                    a.setUlterioriInfo(ulterioriInfo);
                });
            }

            // Carica dati MongoDB per Fasi tramite FaseService
            if (sezione22.getFase() != null && !sezione22.getFase().isEmpty()) {
                sezione22.getFase().forEach(faseService::loadMongoDataForFase);
            }

            log.debug("Dati MongoDB caricati per Sezione22 id={}", sezione22.getId());
            return sezione22;
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per Sezione22 id={}: {}", sezione22.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB Sezione22", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(Sezione22DTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione22 entity = sezione22Mapper.toEntity(request, context);
            // Salvo lo stato dell'oggetto per un eventuale rollback
            if (entity.getId() != null) {
                sezione22Repository.findById(entity.getId()).ifPresent(existing ->
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione22.class, existing))
                );
            }
            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());

            Sezione22 savedEntity = sezione22Repository.save(entity);

            // Gestione storico stato: evita duplicazioni se lo stato non cambia
            String statoCorrenteStorico = entity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                    entity.getId(),
                    Sezione.SEZIONE_22.name()
                )
            )
                : null;

            String nuovoStatoName = statoEnum.name();

            if (!nuovoStatoName.equals(statoCorrenteStorico)) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(
                            StatoSezione.builder()
                                .id(statoEnum.getId())
                                .testo(statoEnum.getDescrizione())
                                .build()
                        )
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_22.name())
                        .testo(statoEnum.getDescrizione())
                        .build()
                );
            }

            Sezione22DTO response = sezione22Mapper.toDto(savedEntity, context);

            // Salvataggio batch dei figli
            if (request.getObbiettiviPerformance() != null && !request.getObbiettiviPerformance().isEmpty()) {
                obbiettivoPerformanceService.saveOrUpdateAll(request.getObbiettiviPerformance());
            }

            if (request.getFase() != null && !request.getFase().isEmpty()) {
                faseService.saveOrUpdateAll(request.getFase());
            }

            if (request.getAdempimenti() != null && !request.getAdempimenti().isEmpty()) {
                adempimentoService.saveOrUpdateAll(request.getAdempimenti());
            }

            //Allegati
            if (request.getAllegati() != null && !request.getAllegati().isEmpty()) {
                List<Allegato> allegati = request.getAllegati().stream()
                    .filter(a -> a.getId() != null && allegatoRepository.existsById(a.getId()))
                    .map(dto -> commonMapper.allegatoDtoToEntity(dto, context)).toList();

                allegatoRepository.saveAll(allegati);
            }

            // Salva i dati MongoDB tramite metodo dedicato
            saveMongoDataSezione22(response, request);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione22 per id={}: {}",
                request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(sezione22Mapper.toEntity(request, context), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione22", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione22DTO richiediValidazione(Long id) {
        log.info("Richiesta validazione stato Sezione22 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione22 entity = sezione22Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione22 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione22 saved = sezione22Repository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(

                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_22.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build()
            );

            Sezione22DTO response = sezione22Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione22 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione22", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione22DTO findByIdPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            log.debug("Ricerca Sezione22 per idPiao: {}", idPiao);
            Optional<Sezione22> sezione22Opt = sezione22Repository.findByPiaoId(idPiao);

            if (sezione22Opt.isEmpty()) {
                log.warn("Sezione22 non trovata per idPiao: {}", idPiao);
                return null;
            }

            Sezione22 sezione22 = sezione22Opt.get();
            log.info("Sezione22 trovata con id: {} per idPiao: {}", sezione22.getId(), idPiao);

            // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
            Sezione22DTO response = sezione22Mapper.toDto(sezione22, context);

            // Recupero lo stato della singola sezione
            response.setStatoSezione(StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sezione22.getId(), Sezione.SEZIONE_22.name())
            ));

            // Carica SOLO i dati MongoDB della sezione22
            response = loadMongoDataSezione22(response);

            // Carica i dati MongoDB per ogni Obiettivo Performance
            if (response.getObbiettiviPerformance() != null && !response.getObbiettiviPerformance().isEmpty()) {
                response.getObbiettiviPerformance().forEach(obbiettivoPerformanceService::loadMongoDataForObiettivo);
            }

            return response;
        } catch (Exception e) {
            log.error("Errore nella ricerca di Sezione22 per idPiao {}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca della Sezione22", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoDataSezione22(Sezione22DTO response, Sezione22DTO request) {
        if (response == null || response.getId() == null || request == null) {
            log.warn("Response, Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo MongoDB
            response.setUlterioriInfo(
                Optional.ofNullable(request.getUlterioriInfo())
                    .map(dto -> {
                        UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(dto, context);
                        entityMongo.setExternalId(response.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtil.saveItem(
                        e,
                        ulterioriInfoRepository,
                        UlterioriInfo.class,
                        en -> en.setTipoSezione(Sezione.SEZIONE_22)
                    ))
                    .map(saved -> commonMapper.ulterioriInfoEntityToDto(saved, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB salvati per Sezione22 id={}", response.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione22 id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Sezione22", e);
        }
    }
}

