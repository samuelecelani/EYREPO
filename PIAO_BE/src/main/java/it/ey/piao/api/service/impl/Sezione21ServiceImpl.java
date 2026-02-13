package it.ey.piao.api.service.impl;


import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.OVPMapper;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione21Mapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.*;
import it.ey.piao.api.service.IOVPService;
import it.ey.piao.api.service.ISezione21Service;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.SezioneUtils;
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
import java.util.stream.Collectors;

@Service
public class Sezione21ServiceImpl implements ISezione21Service {

    private final ISezione21Repository sezione21Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final CommonMapper commonMapper; // MapStruct per entità comuni
    private final Sezione21Mapper sezione21Mapper; // MapStruct mapper per relazioni annidate
    private final PiaoMapper piaoMapper;
    private final OVPMapper ovpMapper;
    private final ISwotPuntiForzaRepository swotPuntiForzaRepository;
    private final ISwotPuntiDebolezzaRepository swotPuntiDebolezzaRepository;
    private final ISwotOpportunitaRepository swotOpportunitaRepository;
    private final ISwotMinacceRepository swotMinacceRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final AllegatoRepository allegatoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoUtils mongoUtil;
    private final IOVPService ovpService;

    private static final Logger log = LoggerFactory.getLogger(Sezione21ServiceImpl.class);

    public Sezione21ServiceImpl(ISezione21Repository sezione21Repository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, CommonMapper commonMapper, Sezione21Mapper sezione21Mapper, PiaoMapper piaoMapper, OVPMapper ovpMapper, ISwotPuntiForzaRepository swotPuntiForzaRepository, ISwotPuntiDebolezzaRepository swotPuntiDebolezzaRepository, ISwotOpportunitaRepository swotOpportunitaRepository, ISwotMinacceRepository swotMinacceRepository, IUlterioriInfoRepository ulterioriInfoRepository, AllegatoRepository allegatoRepository, ApplicationEventPublisher eventPublisher, MongoUtils mongoUtil, IOVPService ovpService) {
        this.sezione21Repository = sezione21Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.commonMapper = commonMapper;
        this.sezione21Mapper = sezione21Mapper;
        this.piaoMapper = piaoMapper;
        this.ovpMapper = ovpMapper;
        this.swotPuntiForzaRepository = swotPuntiForzaRepository;
        this.swotPuntiDebolezzaRepository = swotPuntiDebolezzaRepository;
        this.swotOpportunitaRepository = swotOpportunitaRepository;
        this.swotMinacceRepository = swotMinacceRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.allegatoRepository = allegatoRepository;
        this.eventPublisher = eventPublisher;
        this.mongoUtil = mongoUtil;
        this.ovpService = ovpService;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione21DTO getOrCreateSezione21(PiaoDTO piao) {
        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione21 dal repository usando il Piao
            Sezione21 existing = sezione21Repository.findByPiao(piaoMapper.toEntity(piao, context));

            if (existing != null) {
                // USA MAPSTRUCT invece di GenericMapper per mappare correttamente le relazioni annidate
                // MapStruct mappa correttamente: Sezione21 → OVP → Strategie → Indicatori
                Sezione21DTO response = sezione21Mapper.toDto(existing, context);

                //Recupero lo stato della singola sezione
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_21.name())));

                // Carica SOLO i dati MongoDB della sezione21
                response = loadMongoDataSezione21(response);

                // Carica i dati MongoDB per ogni OVP (indicatori con UlterioriInfo MongoDB)
                if (response.getOvp() != null && !response.getOvp().isEmpty()) {
                    response.getOvp().forEach(ovpService::loadMongoDataForOVP);
                }

                log.info("Sezione21 trovata per PIAO id={} con {} OVP", piao.getId(),
                    response.getOvp() != null ? response.getOvp().size() : 0);
                return response;
            }

            // Creazione nuova Sezione21 se non esiste
            log.info("Nessuna Sezione21 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione21 nuova = Sezione21.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .build();

            Sezione21 salvata = sezione21Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_21.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .build());

            Sezione21DTO response = commonMapper.sezione21ToDto(salvata, context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione21 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione21", e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Sezione21DTO loadMongoDataSezione21(Sezione21DTO sezione21) {
        if (sezione21 == null ) {
            log.warn("Sezione21DTO o idSezione21 è null, skip caricamento MongoDB");
            return sezione21;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Caricamento dati MongoDB
            sezione21.setSwotPuntiForza(
                Optional.ofNullable(swotPuntiForzaRepository.getByExternalId(sezione21.getId()))
                    .map(e -> commonMapper.swotPuntiForzaEntityToDto(e, context))
                    .orElse(null)
            );

            sezione21.setSwotPuntiDebolezza(
                Optional.ofNullable(swotPuntiDebolezzaRepository.getByExternalId(sezione21.getId()))
                    .map(e -> commonMapper.swotPuntiDebolezzaEntityToDto(e, context))
                    .orElse(null)
            );

            sezione21.setSwotOpportunita(
                Optional.ofNullable(swotOpportunitaRepository.getByExternalId(sezione21.getId()))
                    .map(e -> commonMapper.swotOpportunitaEntityToDto(e, context))
                    .orElse(null)
            );

            sezione21.setSwotMinacce(
                Optional.ofNullable(swotMinacceRepository.getByExternalId(sezione21.getId()))
                    .map(e -> commonMapper.swotMinacceEntityToDto(e, context))
                    .orElse(null)
            );

            sezione21.setUlterioriInfo(
                Optional.ofNullable(
                        ulterioriInfoRepository.findByExternalIdAndTipoSezione(sezione21.getId(), Sezione.SEZIONE_21)
                    )
                    .map(e -> commonMapper.ulterioriInfoEntityToDto(e, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB caricati per Sezione21 id={}", sezione21.getId());
            return sezione21;
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per Sezione21 id={}: {}", sezione21.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB Sezione21", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione21DTO saveOrUpdate(Sezione21DTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        Sezione21DTO response;

        try {
            // Sanifica le liste rimuovendo elementi null
            SezioneUtils.sanitizeRequestLists(request);

            Sezione21 entity = sezione21Mapper.toEntity(request, context);
            //Salvo lo stato dell'oggetto per un eventuale rollback
            sezione21Repository.findById(entity.getId()).ifPresent( existing ->
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione21.class,existing))
            );


            // Aggiorno i campi base
            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());

            entity.setIdStato(statoEnum.getId());



            sezione21Repository.findById(request.getId()).ifPresent(s -> eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione21.class, s)));
   // Salvo lo stato dell'oggetto per un eventuale rollback

            //Back Reference per hibernate

            if (entity.getFondiEuropei() != null) {
                entity.getFondiEuropei().forEach(f -> f.setSezione21(entity));
            }
            if(entity.getProcedure() != null) {
                entity.getProcedure().forEach(p -> p.setSezione21(entity));
            }


            Sezione21 savedEntity = sezione21Repository.save(entity);

            // Gestione storico stato: evita duplicazioni se lo stato non cambia
            String statoCorrenteStorico = entity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                    entity.getId(),
                    Sezione.SEZIONE_21.name()
                )
            )
                : null;

            String nuovoStatoName = statoEnum.name();
            StoricoStatoSezione stato = null;

            if (!nuovoStatoName.equals(statoCorrenteStorico)) {
                stato = storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(
                            StatoSezione.builder()
                                .id(statoEnum.getId())
                                .testo(statoEnum.getDescrizione())
                                .build()
                        )
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_21.name())
                        .testo(statoEnum.getDescrizione())
                        .build()
                );
            }

            response = sezione21Mapper.toDto(savedEntity, context);


            if (request.getOvp() != null && !request.getOvp().isEmpty()) {

////                //TODO ELIMINARE APPENA SISTEMATO IL FE
////                List<OVPDTO> updated =  request.getOvp().stream().peek(o -> {
////                    if (o.getSezione21Id() == null){
////                        o.setSezione21Id(savedEntity.getId());
////                    }
//                }).toList();
//                request.setOvp(updated);
                response.setOvp(ovpService.saveOrUpdateAll(request.getOvp()));
            }

            //Allegati
            if (request.getAllegati() != null && !request.getAllegati().isEmpty()) {
                List<Allegato> allegati = request.getAllegati().stream()
                    .filter(a -> a.getId() != null && allegatoRepository.existsById(a.getId()))
                    .map(dto -> commonMapper.allegatoDtoToEntity(dto, context)).toList();

                response.setAllegati(allegatoRepository.saveAll(allegati).stream()
                    .map(e -> commonMapper.allegatoEntityToDto(e, context)).toList());
            } else {
                response.setAllegati(Collections.emptyList());
            }

            // Mappo la lista OVP dalla entity alla response usando enrichWithRelations
            if (entity.getOvpList() != null && !entity.getOvpList().isEmpty()) {
                List<OVPDTO> ovpArricchiti = entity.getOvpList().stream()
                    .map(o-> ovpService.loadMongoDataForOVP(ovpMapper.toDto(o,new CycleAvoidingMappingContext())))
                    .collect(java.util.stream.Collectors.toList());
                response.setOvp(ovpArricchiti);
            }

            response.setStatoSezione(
                stato != null && stato.getStatoSezione() != null
                    ? stato.getStatoSezione().getTesto()
                    : (statoCorrenteStorico != null ? StatoEnum.valueOf(statoCorrenteStorico).getDescrizione() : "")
            );

            // Salva i dati MongoDB tramite metodo dedicato
            saveMongoDataSezione21(response, request);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione21 per id={}: {}", request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(sezione21Mapper.toEntity(request, context), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione21", e);
        }
        return response;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 10)
    public Sezione21DTO richiediValidazione(Long id) {
        log.info("Richiesta validazione stato sezione21 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione21 entity = sezione21Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("sezione21 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione21 saved = sezione21Repository.save(entity);

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(

                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_21.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .build()
            );

            Sezione21DTO response = sezione21Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato sezione21 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato sezione21", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoDataSezione21(Sezione21DTO response, Sezione21DTO request) {
        if (response == null || response.getId() == null || request == null) {
            log.warn("Response, Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva SwotMinacce MongoDB
            response.setSwotMinacce(
                Optional.ofNullable(request.getSwotMinacce())
                    .map(dto -> {
                        SwotMinacce entityMongo = commonMapper.swotMinacceDtoToEntity(dto, context);
                        entityMongo.setExternalId(response.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtil.saveAllItems(e, swotMinacceRepository, SwotMinacce.class))
                    .map(saved -> commonMapper.swotMinacceEntityToDto(saved, context))
                    .orElse(null)
            );

            // Salva SwotOpportunita MongoDB
            response.setSwotOpportunita(
                Optional.ofNullable(request.getSwotOpportunita())
                    .map(dto -> {
                        SwotOpportunita entityMongo = commonMapper.swotOpportunitaDtoToEntity(dto, context);
                        entityMongo.setExternalId(response.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtil.saveAllItems(e, swotOpportunitaRepository, SwotOpportunita.class))
                    .map(saved -> commonMapper.swotOpportunitaEntityToDto(saved, context))
                    .orElse(null)
            );

            // Salva SwotPuntiDebolezza MongoDB
            response.setSwotPuntiDebolezza(
                Optional.ofNullable(request.getSwotPuntiDebolezza())
                    .map(dto -> {
                        SwotPuntiDebolezza entityMongo = commonMapper.swotPuntiDebolezzaDtoToEntity(dto, context);
                        entityMongo.setExternalId(response.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtil.saveAllItems(e, swotPuntiDebolezzaRepository, SwotPuntiDebolezza.class))
                    .map(saved -> commonMapper.swotPuntiDebolezzaEntityToDto(saved, context))
                    .orElse(null)
            );

            // Salva SwotPuntiForza MongoDB
            response.setSwotPuntiForza(
                Optional.ofNullable(request.getSwotPuntiForza())
                    .map(dto -> {
                        SwotPuntiForza entityMongo = commonMapper.swotPuntiForzaDtoToEntity(dto, context);
                        entityMongo.setExternalId(response.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtil.saveAllItems(e, swotPuntiForzaRepository, SwotPuntiForza.class))
                    .map(saved -> commonMapper.swotPuntiForzaEntityToDto(saved, context))
                    .orElse(null)
            );

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
                        en -> en.setTipoSezione(Sezione.SEZIONE_21)
                    ))
                    .map(saved -> commonMapper.ulterioriInfoEntityToDto(saved, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB salvati per Sezione21 id={}", response.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione21 id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Sezione21", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione21DTO findByIdPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            log.debug("Ricerca Sezione21 per idPiao: {}", idPiao);
            Optional<Sezione21> sezione21Opt = sezione21Repository.findByPiaoId(idPiao);

            if (sezione21Opt.isEmpty()) {
                log.warn("Sezione21 non trovata per idPiao: {}", idPiao);
                return null;
            }

            Sezione21 sezione21 = sezione21Opt.get();
            log.info("Sezione21 trovata con id: {} per idPiao: {}", sezione21.getId(), idPiao);

            // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
            Sezione21DTO response = sezione21Mapper.toDto(sezione21, context);

            // Carica SOLO i dati MongoDB della sezione21
            response = loadMongoDataSezione21(response);

            // Carica i dati MongoDB per ogni OVP (indicatori con UlterioriInfo MongoDB)
            if (response.getOvp() != null && !response.getOvp().isEmpty()) {
                response.getOvp().forEach(ovpService::loadMongoDataForOVP);
            }

            return response;
        } catch (Exception e) {
            log.error("Errore nella ricerca di Sezione21 per idPiao {}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante la ricerca della Sezione21", e);
        }
    }


}
