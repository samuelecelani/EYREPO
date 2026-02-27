package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione4Mapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.ISezione4Service;
import it.ey.piao.api.service.ISottofaseMonitoraggioService;
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

import java.util.Optional;

@Service
public class Sezione4ServiceImpl implements ISezione4Service {

    private final ISezione4Repository sezione4Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final PiaoMapper piaoMapper;
    private final CommonMapper commonMapper;
    private final Sezione4Mapper sezione4Mapper;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoUtils mongoUtil;
    private final IAttoreRepository attoreRepository;
    private  final ISottofaseMonitoraggioService sottofaseMonitoraggioService;

    private static final Logger log = LoggerFactory.getLogger(Sezione4ServiceImpl.class);

    public Sezione4ServiceImpl(ISezione4Repository sezione4Repository,
                               IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                               PiaoMapper piaoMapper,
                               CommonMapper commonMapper,
                               Sezione4Mapper sezione4Mapper,
                               IUlterioriInfoRepository ulterioriInfoRepository,
                               ApplicationEventPublisher eventPublisher,
                               MongoUtils mongoUtil, IAttoreRepository attoreRepository, ISottofaseMonitoraggioService sottofaseMonitoraggioService) {
        this.sezione4Repository = sezione4Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.piaoMapper = piaoMapper;
        this.commonMapper = commonMapper;
        this.sezione4Mapper = sezione4Mapper;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.eventPublisher = eventPublisher;
        this.mongoUtil = mongoUtil;
        this.attoreRepository = attoreRepository;
        this.sottofaseMonitoraggioService = sottofaseMonitoraggioService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione4DTO getOrCreateSezione4(PiaoDTO piao) {
        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione4 dal repository usando il Piao
            Sezione4 existing = sezione4Repository.findByPiao(piaoMapper.toEntity(piao, context));

            if (existing != null) {
                Sezione4DTO response = sezione4Mapper.toDto(existing,new CycleAvoidingMappingContext());
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_4.name())));

                // Carica i dati MongoDB tramite metodo pubblico
                response = loadMongoDataSezione4(response);

                // recupero di tutte le sottofasi monitoraggio
          //      response.setSottofaseMonitoraggio(sottofaseMonitoraggioService.getAllBySezione4(response.getId()));


                log.info("Sezione4 trovata per PIAO id={}", piao.getId());
                return response;
            }

            // Creazione nuova Sezione4 se non esiste
            log.info("Nessuna Sezione4 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione4 salvata = sezione4Repository.save(Sezione4.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .idStato(StatoEnum.DA_COMPILARE.getId())
                .build());

            Sezione4DTO response = sezione4Mapper.toDto(salvata,new CycleAvoidingMappingContext());

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_4.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .build());

            response.setStatoSezione(stato.getStatoSezione().getTesto());
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione4 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione4", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione4DTO loadMongoDataSezione4(Sezione4DTO sezione4) {
        if (sezione4 == null) {
            log.warn("Sezione4DTO è null, skip caricamento MongoDB");
            return sezione4;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Caricamento dati MongoDB - UlterioriInfo
            sezione4.setUlterioriInfo(
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(sezione4.getId(), Sezione.SEZIONE_4))
                    .map(ultInfo -> commonMapper.ulterioriInfoEntityToDto(ultInfo, context))
                    .orElse(null)
            );


            log.debug("Dati MongoDB caricati per Sezione4 id={}", sezione4.getId());
            return sezione4;
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per Sezione4 id={}: {}", sezione4.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB Sezione4", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione4DTO findByPiao(PiaoDTO piao) {
        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione4 existing = sezione4Repository.findByPiao(piaoMapper.toEntity(piao, context));
            if (existing != null) {
                Sezione4DTO response = sezione4Mapper.toDto(existing,new CycleAvoidingMappingContext());
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_4.name())));

                // Carica i dati MongoDB
                response = loadMongoDataSezione4(response);


                // Setto le sottofasi monitoraggio per la sezione 4
                response.setSottofaseMonitoraggio(sottofaseMonitoraggioService.getAllBySezione4(response.getId()));



                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione4 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione4", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione4DTO saveOrUpdate(Sezione4DTO request) {

        Sezione4DTO response;
        try {
            Sezione4 entity = sezione4Mapper.toEntity(request,new CycleAvoidingMappingContext());

            // Salvo lo stato dell'oggetto per un eventuale rollback
            sezione4Repository.findById(entity.getId()).ifPresent(existing ->
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione4.class, existing))
            );

            // Setto solo id stato per recupero storico
            entity.setIdStato(StatoEnum.fromDescrizione(request.getStatoSezione()).getId());

            Sezione4 savedEntity = sezione4Repository.save(entity);
            response = sezione4Mapper.toDto(savedEntity,new CycleAvoidingMappingContext());

            // Gestione storico stato sezione
            String currentState = StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(request.getId(), Sezione.SEZIONE_4.name()));

            if (!StatoEnum.fromDescrizione(request.getStatoSezione()).name().equals(currentState)) {
                StoricoStatoSezione stato = storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(StatoSezione.builder()
                            .id(StatoEnum.fromDescrizione(request.getStatoSezione()).getId())
                            .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                            .build())
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_4.name())
                        .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                        .build());
                response.setStatoSezione(stato.getStatoSezione().getTesto());
            } else {
                response.setStatoSezione(request.getStatoSezione());
            }

            // Salvataggio dati MongoDB
            saveMongoDataSezione4(response, request);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
            log.info("Sezione4 salvata/aggiornata con id={}", response.getId());

            return response;

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione4: {}", e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(sezione4Mapper.toEntity(request, new CycleAvoidingMappingContext()),e));

            throw new RuntimeException("Errore durante il salvataggio della Sezione4", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoDataSezione4(Sezione4DTO response, Sezione4DTO request) {
        if (response == null || response.getId() == null || request == null) {
            log.warn("Response, Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo MongoDB (prende da request, setta su response)
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
                        en -> en.setTipoSezione(Sezione.SEZIONE_4)
                    ))
                    .map(saved -> commonMapper.ulterioriInfoEntityToDto(saved, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB salvati per Sezione4 id={}", response.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione4 id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Sezione4", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione4DTO richiediValidazione(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID della Sezione4 non può essere nullo.");
        }

        try {
            Sezione4 existing = sezione4Repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sezione4 non trovata con id=" + id));

            // Aggiorna lo stato a IN_VALIDAZIONE
            existing.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione4 savedEntity = sezione4Repository.save(existing);

            Sezione4DTO response = sezione4Mapper.toDto(savedEntity,new CycleAvoidingMappingContext());

            // Salva il nuovo stato nello storico
            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder()
                        .id(StatoEnum.IN_VALIDAZIONE.getId())
                        .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                        .build())
                    .idEntitaFK(savedEntity.getId())
                    .codTipologiaFK(Sezione.SEZIONE_4.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build());

            response.setStatoSezione(stato.getStatoSezione().getTesto());

            log.info("Richiesta validazione per Sezione4 id={}", id);
            return response;

        } catch (Exception e) {
            log.error("Errore durante richiediValidazione Sezione4 id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la richiesta di validazione della Sezione4", e);
        }
    }
}
