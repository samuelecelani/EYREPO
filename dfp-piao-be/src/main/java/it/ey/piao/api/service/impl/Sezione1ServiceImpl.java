package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione1Mapper;
import it.ey.piao.api.mapper.StakeHolderMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.ISocialRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.ISezione1Service;
import it.ey.piao.api.service.IAnagraficaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.SezioneUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class Sezione1ServiceImpl implements ISezione1Service {

    private final ISezione1Repository sezione1Repository;
    private final AllegatoRepository allegatoRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final StakeHolderMapper    stakeHolderMapper; // MapStruct per entità comuni
    private final PiaoMapper piaoMapper;
    private final CommonMapper commonMapper;
    private final Sezione1Mapper sezione1Mapper; // MapStruct mapper
    private final ISocialRepository socialRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoUtils mongoUtil;
    private final IStakeHolderRepository stakeHolderRepository;
    private final PiaoRepository piaoRepository;
    private final StoricoModificaHelper storicoModificaHelper;

    private final IAnagraficaService anagraficaService;

    private static final Logger log = LoggerFactory.getLogger(Sezione1ServiceImpl.class);

    public Sezione1ServiceImpl(ISezione1Repository sezione1Repository, AllegatoRepository allegatoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, StakeHolderMapper stakeHolderMapper, PiaoMapper piaoMapper, CommonMapper commonMapper, Sezione1Mapper sezione1Mapper, ISocialRepository socialRepository, IUlterioriInfoRepository ulterioriInfoRepository, ApplicationEventPublisher eventPublisher, MongoUtils mongoUtil, IStakeHolderRepository stakeHolderRepository, PiaoRepository piaoRepository, StoricoModificaHelper storicoModificaHelper, IAnagraficaService anagraficaService) {
        this.sezione1Repository = sezione1Repository;
        this.allegatoRepository = allegatoRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.stakeHolderMapper = stakeHolderMapper;
        this.piaoMapper = piaoMapper;
        this.commonMapper = commonMapper;
        this.sezione1Mapper = sezione1Mapper;
        this.socialRepository = socialRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.eventPublisher = eventPublisher;
        this.mongoUtil = mongoUtil;
        this.stakeHolderRepository = stakeHolderRepository;
        this.piaoRepository = piaoRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.anagraficaService = anagraficaService;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione1DTO getOrCreateSezione(PiaoDTO piao) {
        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione1 dal repository usando il Piao
            Sezione1 existing = sezione1Repository.findByIdPiao(piao.getId());

            if (existing != null ) {

                Sezione1DTO response = sezione1Mapper.toDto(existing, context);
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_1.name())));

                response.setStakeHolders(getStakeHolderByPiao(piao.getId()));

                // Carica i dati MongoDB tramite metodo pubblico
                response = loadMongoData(response);




                log.info("Sezione1 trovata per PIAO id={}", piao.getId());
                return response;
            }

            // Creazione nuova Sezione1 se non esiste
            log.info("Nessuna Sezione1 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione1 salvata = sezione1Repository.save(Sezione1.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .idStato(StatoEnum.DA_COMPILARE.getId())
                .build());

            Sezione1DTO response = sezione1Mapper.toDto(salvata, context);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_1.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .createdByNameSurname(piao.getCreatedByNameSurname())
                .createdByRole(piao.getCreatedByRole())
                .build());

            response.setStatoSezione(stato.getStatoSezione().getTesto());
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione1 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione1", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione1DTO loadMongoData(Sezione1DTO sezione1) {
        if (sezione1 == null ) {
            log.warn("Sezione1DTO o idSezione1 è null, skip caricamento MongoDB");
            return sezione1;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {

            sezione1.setUlterioriInfo(
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(sezione1.getId(), Sezione.SEZIONE_1))
                    .map(ultInfo -> commonMapper.ulterioriInfoEntityToDto(ultInfo, context))
                    .orElse(null)
            );



            log.debug("Dati MongoDB caricati per Sezione1 id={}", sezione1.getId());
            return sezione1;
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per Sezione1 id={}: {}", sezione1.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB Sezione1", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione1DTO findByIdPiao( Long idPiao) {
        if (idPiao == null ) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione1 existing = sezione1Repository.findByIdPiao(idPiao);
            if (existing != null) {
                Sezione1DTO response = sezione1Mapper.toDto(existing, context);
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_1.name())));

                //Carica StakeHolder
                response.setStakeHolders(getStakeHolderByPiao(idPiao));

                // Carica i dati MongoDB
                response = loadMongoData(response);


                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione1 per PIAO id={}: {}",idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione1", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(Sezione1DTO request) {
        // Pulisci le liste figlie nella DTO rimuovendo elementi nulli o "vuoti"
        Sezione1DTO sezione1Save= SezioneUtils.sanitizeChildLists(request);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione1 entity = sezione1Mapper.toEntity(sezione1Save, context);
            //Salvo lo stato dell'oggetto per un eventuale rollback
            sezione1Repository.findById(entity.getId()).ifPresent( existing ->
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione1.class,existing))
            );
            //Setto solo id stato per recupero storico
            entity.setIdStato(StatoEnum.fromDescrizione(request.getStatoSezione()).getId());

            Sezione1 savedEntity = sezione1Repository.save(entity);

            if (!StatoEnum.fromDescrizione(request.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(request.getId(),Sezione.SEZIONE_1.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(request.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_1.name())
                        .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build());
            }

            if (request.getAllegati() != null && !request.getAllegati().isEmpty()) {
                List<Allegato> allegati = request.getAllegati().stream()
                    .filter(a -> a.getId() !=null && allegatoRepository.existsById(a.getId()))
                    .map(dto -> commonMapper.allegatoDtoToEntity(dto, context)).toList();
                allegatoRepository.saveAll(allegati);
            }

            if (request.getStakeHolders() != null && !request.getStakeHolders().isEmpty()) {
                List<StakeHolder> stakeHolders = request.getStakeHolders().stream()
                    .map(stakeHolderDTO -> {
                        var stakeHolder = stakeHolderMapper.toEntity(stakeHolderDTO, context);
                        stakeHolder.setPiao(piaoRepository.getReferenceById(request.getIdPiao()));
                        return stakeHolder;
                    })
                    .toList();
                stakeHolderRepository.saveAll(stakeHolders);
            }

            // Salva/aggiorna l'Anagrafica collegata alla Sezione1
            if (request.getAnagrafica() != null) {
                AnagraficaDTO anagraficaDTO = request.getAnagrafica();
                anagraficaDTO.setIdSezione1(savedEntity.getId());
                anagraficaService.save(anagraficaDTO);
            }

            // Salva i dati MongoDB tramite metodo dedicato
            saveMongoData(request);

            // Salva storico modifica se presente campiModificati
            storicoModificaHelper.salvaStoricoSePresente(request, savedEntity.getId(), request.getIdPiao(), Sezione.SEZIONE_1);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedEntity));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione1 per id={}: {}", request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(sezione1Mapper.toEntity(request, context),e));
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione1", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione1DTO richiediValidazione(Long id, String userNameSurname, String userRole,String fiscalCode,String testoSezione,String campiModificati) {
        log.info("Richiesta validazione stato Sezione1 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione1 entity = sezione1Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione1 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione1 saved = sezione1Repository.save(entity);
          Sezione1DTO dto =   sezione1Mapper.toDto(saved,new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(

                        StatoSezione.builder()
                            .id(StatoEnum.IN_VALIDAZIONE.getId())
                            .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_1.name())
                    .testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );
            Sezione1DTO response = sezione1Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            // Salva storico modifica se presente campiModificati
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_1);
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione1 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione1", e);
        }
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveMongoData(Sezione1DTO request) {
        if (request == null) {
            log.warn("Request è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo MongoDB
            UlterioriInfo ulterioriInfoEntity = request.getUlterioriInfo() != null
                ? commonMapper.ulterioriInfoDtoToEntity(request.getUlterioriInfo(), context)
                : null;
            if (ulterioriInfoEntity != null) ulterioriInfoEntity.setExternalId(request.getId());
            mongoUtil.saveItem(ulterioriInfoEntity, request.getId(),
                ulterioriInfoRepository, UlterioriInfo.class,
                en -> en.setTipoSezione(Sezione.SEZIONE_1),
                "tipoSezione", Sezione.SEZIONE_1);


            log.debug("Dati MongoDB salvati per Sezione1 id={}", request.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione1 id={}: {}", request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Sezione1", e);
        }
    }

    private List<StakeHolderDTO> getStakeHolderByPiao(Long idPiao) {
        try {
            return stakeHolderMapper.toDtoList(stakeHolderRepository.findByIdPiao(idPiao),new CycleAvoidingMappingContext());

        } catch (Exception e) {
            log.error("Errore recupero StakeHolder per PIAO id={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore recupero StakeHolder per PIAO", e);
        }
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione1DTO validaSezione(Long id, String userNameSurname, String userRole,String fiscalCode,String testoSezione,String campiModificati) {
        log.info("Validazione stato Sezione1 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
           // Recupero la sezione da DB
            Sezione1 entity = sezione1Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione1 non trovata"));

            //  si valida solo se in validazione
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per validare la sezione deve essere IN_VALIDAZIONE");
            }

            // aggiorniamo lostato a validata
            entity.setIdStato(StatoEnum.VALIDATA.getId());
            // salviamo su DB la sezione con il nuovo stato
            Sezione1 saved = sezione1Repository.save(entity);
            Sezione1DTO dto =   sezione1Mapper.toDto(saved,new CycleAvoidingMappingContext());
            //Inserisco una nuova riga nello storico con lo stato VALIDATA
            //nessun flag perchè andiamo a validare la sezione

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.VALIDATA.getId())
                            .testo(StatoEnum.VALIDATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_1.name())
                    .testo(StatoEnum.VALIDATA.getDescrizione())
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione1DTO response = sezione1Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.VALIDATA.getDescrizione());

            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_1);
            return response;

        } catch (Exception e) {
            log.error("Errore validazione Sezione1 {}", e.getMessage(), e);
            throw new RuntimeException("Errore validazione Sezione1", e);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione1DTO rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole,String fiscalCode,String testoSezione,String campiModificati) {
        log.info("Rifiuto validazione stato Sezione1 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {

            if (osservazioni ==null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            //  Recupero la sezione da DB
            Sezione1 entity = sezione1Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione1 non trovata"));

            //  si può rifiutare SOLO se la sezione è IN_VALIDAZIONE
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per rifiutare la validazione la sezione deve essere IN_VALIDAZIONE"
                );
            }

            // Aggiorno lo stato della sezione a COMPILATA ,quindi si ritorna al vecchio stato
            entity.setIdStato(StatoEnum.COMPILATA.getId());

            // salviamo su DB la sezione con il nuovo stato
            Sezione1 saved = sezione1Repository.save(entity);
            Sezione1DTO dto =   sezione1Mapper.toDto(saved,new CycleAvoidingMappingContext());
            //  Inserisco una nuova riga nello storico:
            // questa avrà lo stato COMPILATO e il flag RIFIUTATO a "TRUE"
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_1.name())
                    .testo(StatoEnum.COMPILATA.getDescrizione())
                    .rifiutato(Boolean.TRUE)
                    .revocato(Boolean.FALSE)
                    .annullato(Boolean.FALSE)
                    .osservazioni(osservazioni)
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione1DTO response = sezione1Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_1);
            return response;

        } catch (Exception e) {
            log.error("Errore rifiuto validazione Sezione1 {}", e.getMessage(), e);
            throw new RuntimeException("Errore rifiuto validazione Sezione1", e);
        }
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione1DTO revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole,String fiscalCode,String testoSezione,String campiModificati) {
        log.info("Revoca validazione stato Sezione1 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            if (osservazioni ==null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            // Recupero la sezione da DB
            Sezione1 entity = sezione1Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione1 non trovata"));

            //  possiamo revocare SOLO se lo stato corrente è VALIDATA
            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per revocare la validazione la sezione deve essere VALIDATA"
                );
            }

            //  Cambio stato: VALIDATA -> COMPILATA (revoca della validazione)
            entity.setIdStato(StatoEnum.COMPILATA.getId());

            //  Persisto la modifica su DB
            Sezione1 saved = sezione1Repository.save(entity);
            Sezione1DTO dto =   sezione1Mapper.toDto(saved,new CycleAvoidingMappingContext());
            // Inserisco una nuova riga nello storico:
            // che avrà lo stato in Compilata e il flag REVOCATA a "TRUE"
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_1.name())
                    .testo(StatoEnum.COMPILATA.getDescrizione())
                    .revocato(Boolean.TRUE)
                    .rifiutato(Boolean.FALSE)
                    .annullato(Boolean.FALSE)
                    .osservazioni(osservazioni)
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione1DTO response = sezione1Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_1);
            return response;

        } catch (Exception e) {
            log.error("Errore revoca validazione Sezione1 {}", e.getMessage(), e);
            throw new RuntimeException("Errore revoca validazione Sezione1", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione1DTO annullaValidazione(Long id, String userNameSurname, String userRole,String fiscalCode,String testoSezione,String campiModificati) {
            log.info("Annullo validazione stato Sezione1 per id={}", id);
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
            try {
                //  Recupero la sezione da DB
                Sezione1 entity = sezione1Repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sezione1 non trovata"));

                //  si può annullare SOLO se la sezione è IN_VALIDAZIONE
                if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                    throw new IllegalStateException(
                        "Transizione non ammessa: per annullare la validazione la sezione deve essere IN_VALIDAZIONE"
                    );
                }

                // Aggiorno lo stato della sezione a COMPILATA ,quindi si ritorna al vecchio stato
                entity.setIdStato(StatoEnum.COMPILATA.getId());

                // salviamo su DB la sezione con il nuovo stato
                Sezione1 saved = sezione1Repository.save(entity);
                Sezione1DTO dto =   sezione1Mapper.toDto(saved,new CycleAvoidingMappingContext());
                //  Inserisco una nuova riga nello storico:
                // questa avrà lo stato COMPILATO e il flag ANNULLATO a "TRUE"
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.COMPILATA.getId())
                                .testo(StatoEnum.COMPILATA.getDescrizione())
                                .build()
                        )
                        .idEntitaFK(saved.getId())
                        .codTipologiaFK(Sezione.SEZIONE_1.name())
                        .testo(StatoEnum.COMPILATA.getDescrizione())
                        .rifiutato(Boolean.FALSE)
                        .revocato(Boolean.FALSE)
                        .annullato(Boolean.TRUE)
                        .createdBy(fiscalCode)
                        .createdByNameSurname(userNameSurname)
                        .createdByRole(userRole)
                        .build()
                );

                Sezione1DTO response = sezione1Mapper.toDto(saved, context);
                response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
                if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                    dto.setCampiModificati(campiModificati);
                    dto.setTestoSezione(testoSezione);
                }
                storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_1);
                return response;

            } catch (Exception e) {
                log.error("Errore annulla validazione Sezione1 {}", e.getMessage(), e);
                throw new RuntimeException("Errore annulla validazione Sezione1", e);
            }
    }

}
