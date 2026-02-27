package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione23Mapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.AllegatoRepository;
import it.ey.piao.api.repository.ISezione23Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.*;
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
public class Sezione23ServiceImpl implements ISezione23Service {

    private final ISezione23Repository sezione23Repository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommonMapper commonMapper; // MapStruct per entità comuni
    private final PiaoMapper piaoMapper;
    private final Sezione23Mapper sezione23Mapper; // MapStruct mapper
    private final MongoUtils mongoUtil;
    private final AllegatoRepository allegatoRepository;
    private static final Logger log = LoggerFactory.getLogger(Sezione23ServiceImpl.class);
    private final IObiettivoPrevenzioneService obiettivoPrevenzioneService;
    private final IMisuraPrevenzioneService misuraPrevenzioneService;
    private final IObbligoLeggeService obbligoLeggeService;
    private final IAttivitaSensibileService attivitaSensibileService;
    private final IEventoRischioService eventoRischioService;
    private final IAdempimentiNormativiService adempimentiNormativiService;
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaService obiettivoPrevenzioneCorruzioneTrasparenzaService;
    private final IDatiPubblicatiService datiPubblicatiService;
    public Sezione23ServiceImpl(ISezione23Repository sezione23Repository, IUlterioriInfoRepository ulterioriInfoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, ApplicationEventPublisher eventPublisher, CommonMapper commonMapper, PiaoMapper piaoMapper, Sezione23Mapper sezione23Mapper, MongoUtils mongoUtil, AllegatoRepository allegatoRepository, IObiettivoPrevenzioneService obiettivoPrevenzioneService, IMisuraPrevenzioneService misuraPrevenzioneService, IObbligoLeggeService obbligoLeggeService, IAttivitaSensibileService attivitaSensibileService, IEventoRischioService eventoRischioService, IAdempimentiNormativiService adempimentiNormativiService, IObiettivoPrevenzioneCorruzioneTrasparenzaService obiettivoPrevenzioneCorruzioneTrasparenzaService, IDatiPubblicatiService datiPubblicatiService) {
        this.sezione23Repository = sezione23Repository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.eventPublisher = eventPublisher;
        this.commonMapper = commonMapper;
        this.piaoMapper = piaoMapper;
        this.sezione23Mapper = sezione23Mapper;
        this.mongoUtil = mongoUtil;
        this.allegatoRepository = allegatoRepository;
        this.obiettivoPrevenzioneService = obiettivoPrevenzioneService;
        this.misuraPrevenzioneService = misuraPrevenzioneService;
        this.obbligoLeggeService = obbligoLeggeService;
        this.attivitaSensibileService = attivitaSensibileService;
        this.eventoRischioService = eventoRischioService;
        this.adempimentiNormativiService = adempimentiNormativiService;
        this.obiettivoPrevenzioneCorruzioneTrasparenzaService = obiettivoPrevenzioneCorruzioneTrasparenzaService;
        this.datiPubblicatiService = datiPubblicatiService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione23DTO getOrCreateSezione23(PiaoDTO piao) {

        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione23 dal repository usando il Piao
            Sezione23 existing = sezione23Repository.findByIdPiao(piao.getId());

            if (existing != null) {
                // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
                // MapStruct mappa automaticamente: Sezione23 → Obiettivi → Misure → Indicatori
                Sezione23DTO response = sezione23Mapper.toDto(existing, context);

                //Recupero lo stato della singola sezione
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_23.name())));

                // Carica SOLO i dati MongoDB della sezione23
                response = loadMongoDataSezione23(response);



                log.info("Sezione23 trovata per PIAO id={} con {} obiettivi", piao.getId(),
                    response.getObiettivoPrevenzione() != null ? response.getObiettivoPrevenzione().size() : 0);
                return response;
            }

            // Creazione nuova Sezione23 se non esiste
            log.info("Nessuna Sezione23 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione23 nuova = Sezione23.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .build();

            Sezione23 salvata = sezione23Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_23.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .build());

            Sezione23DTO response = sezione23Mapper.toDto(salvata, context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione23 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione23", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione23DTO loadMongoDataSezione23(Sezione23DTO sezione23) {
        if (sezione23 == null ) {
            log.warn("Sezione23DTO o Sezione23 è null, skip caricamento MongoDB");
            return sezione23;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Caricamento dati MongoDB base della sezione23
            sezione23.setUlterioriInfo(
                Optional.ofNullable(
                        ulterioriInfoRepository.findByExternalIdAndTipoSezione(sezione23.getId(), Sezione.SEZIONE_23)
                    )
                    .map(e -> commonMapper.ulterioriInfoEntityToDto(e, context))
                    .orElse(null)
            );

            // Carica i dati MongoDB per ogni Obiettivo Prevenzione
            if (sezione23.getObiettivoPrevenzione() != null && !sezione23.getObiettivoPrevenzione().isEmpty()) {
                sezione23.getObiettivoPrevenzione().forEach(obiettivoDTO -> {
                    obiettivoPrevenzioneService.loadMongoDataForObiettivo(obiettivoDTO);

                    // Carica i dati MongoDB per ogni Misura Prevenzione
                    if (obiettivoDTO.getMisurePrevenzione() != null && !obiettivoDTO.getMisurePrevenzione().isEmpty()) {
                        obiettivoDTO.getMisurePrevenzione().forEach(misuraPrevenzioneService::loadMongoDataForMisura);
                    }
                });
            }
            // Carica i dati MongoDB per ogni Attività Sensibile
            List<AttivitaSensibileDTO> attivitaList = sezione23.getAttivitaSensibile();
            if (attivitaList != null && !attivitaList.isEmpty()) {
                List<AttivitaSensibileDTO> updated = attivitaList.stream()
                    .map(attivita -> {
                        AttivitaSensibileDTO loaded = attivitaSensibileService.loadMongoData(attivita);

                        List<EventoRischioDTO> eventi = loaded.getEventoRischio();
                        if (eventi != null && !eventi.isEmpty()) {
                            loaded.setEventoRischio(
                                eventi.stream()
                                    .map(eventoRischioService::loadMongoData)
                                    .toList()
                            );
                        }
                        return loaded;
                    })
                    .toList();

                sezione23.setAttivitaSensibile(updated);
            }

            // Carica i dati MongoDB per ogni Obbligo Legge
            if (sezione23.getObblighiLegge() != null && !sezione23.getObblighiLegge().isEmpty()) {
                sezione23.getObblighiLegge().forEach(obbligoLeggeService::loadMongoDataForObbligo);
            }

            log.debug("Dati MongoDB caricati per Sezione23 id={}", sezione23.getId());
            return sezione23;
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per Sezione23 id={}: {}", sezione23.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB Sezione23", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(Sezione23DTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Mappo DTO -> Entity
            Sezione23 entity = sezione23Mapper.toEntity(request, context);

            // Evento rollback
            if (entity.getId() != null) {
                sezione23Repository.findById(entity.getId())
                    .ifPresent(existing -> eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione23.class, existing)));
            }

            // Stato
            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());

            // Salvataggio principale
            Sezione23 savedEntity = sezione23Repository.save(entity);

            // Imposto il genitore per gli obblighiLegge
            if (entity.getObblighiLegge() != null) {
                entity.getObblighiLegge().forEach(obbligo -> obbligo.setSezione23(savedEntity));
            }

            if (request.getObblighiLegge() != null && !request.getObblighiLegge().isEmpty()) {
                for (ObbligoLeggeDTO obbligoDTO : request.getObblighiLegge()) {
                    // Salvo sempre l'obbligo
                    obbligoLeggeService.saveOrUpdate(obbligoDTO);


                }
            }

            if (request.getObiettivoPrevenzione() != null && !request.getObiettivoPrevenzione().isEmpty()) {
                obiettivoPrevenzioneService.saveAll(request.getObiettivoPrevenzione());
            }
            if (request.getMisuraPrevenzione() != null && !request.getMisuraPrevenzione().isEmpty()) {
                misuraPrevenzioneService.saveAll(request.getMisuraPrevenzione());
            }

            if (request.getAdempimentiNormativi() != null && !request.getAdempimentiNormativi().isEmpty()) {
                adempimentiNormativiService.saveOrUpdateAll(request.getAdempimentiNormativi());
            }
            if (request.getAttivitaSensibile() != null && !request.getAttivitaSensibile().isEmpty())  {
                attivitaSensibileService.saveOrUpdateAll(request.getAttivitaSensibile());
            }

            if (request.getObiettivoPrevenzioneCorruzioneTrasparenza() != null && !request.getObiettivoPrevenzioneCorruzioneTrasparenza().isEmpty())  {
                obiettivoPrevenzioneCorruzioneTrasparenzaService.saveAll(request.getObiettivoPrevenzioneCorruzioneTrasparenza());
            }

            // Storico stato
            String statoCorrente = entity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(entity.getId(), Sezione.SEZIONE_23.name())
            )
                : null;
            if (!statoEnum.name().equals(statoCorrente)) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(StatoSezione.builder().id(statoEnum.getId()).testo(statoEnum.getDescrizione()).build())
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_23.name())
                        .testo(statoEnum.getDescrizione())
                        .build()
                );
            }

            // Allegati
            if (request.getAllegati() != null) {
                List<Allegato> allegati = request.getAllegati().stream()
                    .filter(a -> a.getId() != null && allegatoRepository.existsById(a.getId()))
                    .map(dto -> commonMapper.allegatoDtoToEntity(dto, context))
                    .toList();
                allegatoRepository.saveAll(allegati);
            }

            // Salva i dati MongoDB → qui passiamo DTO aggiornato con ID JPA
            Sezione23DTO savedDto = sezione23Mapper.toDto(savedEntity, context);
            saveMongoDataSezione23(savedDto);

            // Evento successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedDto));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione23 per id={}: {}", request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(sezione23Mapper.toEntity(request, context), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione23", e);
        }
    }




    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione23DTO richiediValidazione(Long id) {
        log.info("Richiesta validazione stato Sezione23 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione23 entity = sezione23Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione23 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione23 saved = sezione23Repository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(

                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_23.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build()
            );

            Sezione23DTO response = sezione23Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione23 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione23", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione23DTO findByPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione23 existing = sezione23Repository.findByIdPiao(idPiao);
            if (existing != null) {
                // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
                Sezione23DTO response = sezione23Mapper.toDto(existing, context);
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_23.name())));

                // Carica i dati MongoDB
                response = loadMongoDataSezione23(response);

                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione23 per PIAO id={}: {}",idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione23", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoDataSezione23( Sezione23DTO request) {
        if ( request == null) {
            log.warn("Response, Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo MongoDB

                Optional.ofNullable(request.getUlterioriInfo())
                    .map(dto -> {
                        UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(dto, context);
                        entityMongo.setExternalId(request.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtil.saveItem(
                        e,
                        ulterioriInfoRepository,
                        UlterioriInfo.class,
                        en -> en.setTipoSezione(Sezione.SEZIONE_23)
                    ))
                    .map(saved -> commonMapper.ulterioriInfoEntityToDto(saved, context));
            log.debug("Dati MongoDB salvati per Sezione23 id={}", request.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione23 id={}: {}", request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Sezione23", e);
        }
    }
}
