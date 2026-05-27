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
import it.ey.piao.api.service.ICategoriaObiettiviService;
import it.ey.piao.api.service.ISezione4Service;
import it.ey.piao.api.service.ISottofaseMonitoraggioService;
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

import java.util.Objects;
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
    private final ICategoriaObiettiviService categoriaObiettiviService;
    private final StoricoModificaHelper storicoModificaHelper;

    private static final Logger log = LoggerFactory.getLogger(Sezione4ServiceImpl.class);

    public Sezione4ServiceImpl(ISezione4Repository sezione4Repository,
                               IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                               PiaoMapper piaoMapper,
                               CommonMapper commonMapper,
                               Sezione4Mapper sezione4Mapper,
                               IUlterioriInfoRepository ulterioriInfoRepository,
                               ApplicationEventPublisher eventPublisher,
                               MongoUtils mongoUtil, IAttoreRepository attoreRepository,
                               ISottofaseMonitoraggioService sottofaseMonitoraggioService,
                               ICategoriaObiettiviService categoriaObiettiviService,
                               StoricoModificaHelper storicoModificaHelper) {
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
        this.categoriaObiettiviService = categoriaObiettiviService;
        this.storicoModificaHelper = storicoModificaHelper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione4DTO getOrCreateSezione(PiaoDTO piao) {
        if (piao == null || piao.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione4 dal repository usando il Piao
            Sezione4 existing = sezione4Repository.findByIdPiao(piao.getId());

            if (existing != null) {
                Sezione4DTO response = sezione4Mapper.toDto(existing,new CycleAvoidingMappingContext());
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_4.name())));

                // Carica i dati MongoDB tramite metodo pubblico
                response = loadMongoData(response);

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
                .createdByNameSurname(piao.getCreatedByNameSurname())
                .createdByRole(piao.getCreatedByRole())
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
    public Sezione4DTO findByIdPiao(Long idPiao)
    {
        if (idPiao == null)
        {
            throw new IllegalArgumentException("L'idPiao non può essere nullo");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione4 existing = sezione4Repository.findByIdPiao(idPiao);
            if (existing != null) {
                Sezione4DTO response = sezione4Mapper.toDto(existing,new CycleAvoidingMappingContext());
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_4.name())));

                // Carica i dati MongoDB
                response = loadMongoData(response);

                // Setto le sottofasi monitoraggio per la sezione 4
                response.setSottofaseMonitoraggio(sottofaseMonitoraggioService.getAllBySezione4(response.getId()));

                // Setto i categoria obiettivi per la sezione 4
                response.setCategoriaObiettivi(categoriaObiettiviService.getAllBySezione4(response.getId()));

                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByIdPiao Sezione4 per PIAO id={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione4", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione4DTO loadMongoData(Sezione4DTO sezione4) {
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
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(Sezione4DTO request) {

        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("Request o ID null: impossibile aggiornare Sezione4");
        }

        try {
            // Recupero entità esistente, lancia eccezione se non trovata
            Sezione4 existing = sezione4Repository.findById(request.getId())
                .orElseThrow(() ->
                    new IllegalArgumentException("Sezione4 non trovata con id=" + request.getId())
                );

            // Pubblica evento prima dell'update
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione4.class, existing));

            // Aggiornamento campi dall'oggetto DTO
            existing.setDescrStrumenti(request.getDescrStrumenti());
            existing.setDescrModalitaRilevazione(request.getDescrModalitaRilevazione());
            existing.setIntro(request.getIntro());
            existing.setIntro21(request.getIntro21());
            existing.setIntro22(request.getIntro22());
            existing.setDescr22(request.getDescr22());
            existing.setDescr23(request.getDescr23());
            existing.setDescr31(request.getDescr31());
            existing.setDescr32(request.getDescr32());
            existing.setDescr331(request.getDescr331());
            existing.setDescr332(request.getDescr332());
            existing.setDescrMonitoraggio(request.getDescrMonitoraggio());

            // Aggiorna lo stato della sezione
            StatoEnum newStateEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            existing.setIdStato(newStateEnum.getId());

            // Salvataggio dell'entità aggiornata
            Sezione4 savedEntity = sezione4Repository.save(existing);

            // Salvataggio delle sottofasi di monitoraggio, se presenti
            if (request.getSottofaseMonitoraggio() != null &&
                !request.getSottofaseMonitoraggio().isEmpty()) {

                for (SottofaseMonitoraggioDTO sfDto : request.getSottofaseMonitoraggio()) {
                    sfDto.setIdSezione4(savedEntity.getId()); // garantisce la relazione
                    sottofaseMonitoraggioService.saveOrUpdate(sfDto);
                }
            }

            // Controllo e aggiornamento dello storico stato se necessario
            String currentState = StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                    request.getId(),
                    Sezione.SEZIONE_4.name()
                )
            );

            if (!newStateEnum.name().equals(currentState)) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(
                            StatoSezione.builder()
                                .id(newStateEnum.getId())
                                .testo(newStateEnum.getDescrizione())
                                .build()
                        )
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_4.name())
                        .testo(newStateEnum.getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build()
                );
            }

            // Salvataggio dei dati su MongoDB
            saveMongoData(request);

            // Salva storico modifica se presente campiModificati
            storicoModificaHelper.salvaStoricoSePresente(request, request.getId(), request.getIdPiao(), Sezione.SEZIONE_4);

            // Pubblica evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(request));
            log.info("Sezione4 salvata/aggiornata con id={}", request.getId());

        } catch (Exception e) {
            // Gestione eccezioni e pubblicazione evento di fallimento
            log.error("Errore durante saveOrUpdate Sezione4: {}", e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new Sezione4(), e));
            throw new RuntimeException("Errore durante il salvataggio della Sezione4", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoData(Sezione4DTO request) {
        if (request == null || request.getId() == null) {
            log.warn("Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo MongoDB (prende da request)
            UlterioriInfo ulterioriInfoEntity = request.getUlterioriInfo() != null
                ? commonMapper.ulterioriInfoDtoToEntity(request.getUlterioriInfo(), context)
                : null;
            if (ulterioriInfoEntity != null) ulterioriInfoEntity.setExternalId(request.getId());
            mongoUtil.saveItem(ulterioriInfoEntity, request.getId(),
                ulterioriInfoRepository, UlterioriInfo.class,
                en -> en.setTipoSezione(Sezione.SEZIONE_4),
                "tipoSezione", Sezione.SEZIONE_4);

            log.debug("Dati MongoDB salvati per Sezione4 id={}", request.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione4 id={}: {}", request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Sezione4", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione4DTO richiediValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        if (id == null) throw new IllegalArgumentException("L'ID della Sezione4 non può essere nullo.");
        try {
            Sezione4 existing = sezione4Repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sezione4 non trovata con id=" + id));
            existing.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione4 savedEntity = sezione4Repository.save(existing);
            Sezione4DTO dto = sezione4Mapper.toDto(savedEntity, new CycleAvoidingMappingContext());
            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.IN_VALIDAZIONE.getId()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione()).build())
                .idEntitaFK(savedEntity.getId()).codTipologiaFK(Sezione.SEZIONE_4.name()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione4DTO response = sezione4Mapper.toDto(savedEntity, new CycleAvoidingMappingContext());
            response.setStatoSezione(stato.getStatoSezione().getTesto());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, savedEntity.getId(), savedEntity.getPiao().getId(), Sezione.SEZIONE_4);
            log.info("Richiesta validazione per Sezione4 id={}", id);
            return response;
        } catch (Exception e) { log.error("Errore durante richiediValidazione Sezione4 id={}: {}", id, e.getMessage(), e); throw new RuntimeException("Errore durante la richiesta di validazione della Sezione4", e); }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione4DTO validaSezione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Validazione stato Sezione4 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione4 entity = sezione4Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione4 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per validare la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.VALIDATA.getId());
            Sezione4 saved = sezione4Repository.save(entity);
            Sezione4DTO dto = sezione4Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.VALIDATA.getId()).testo(StatoEnum.VALIDATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_4.name()).testo(StatoEnum.VALIDATA.getDescrizione())
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione4DTO response = sezione4Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.VALIDATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_4);
            return response;
        } catch (Exception e) { log.error("Errore validazione Sezione4 {}", e.getMessage(), e); throw new RuntimeException("Errore validazione Sezione4", e); }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione4DTO rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Rifiuto validazione stato Sezione4 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni==null || osservazioni.isBlank()) throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            Sezione4 entity = sezione4Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione4 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per rifiutare la validazione la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione4 saved = sezione4Repository.save(entity);
            Sezione4DTO dto = sezione4Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_4.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .rifiutato(Boolean.TRUE).revocato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione4DTO response = sezione4Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_4);
            return response;
        } catch (Exception e) { log.error("Errore rifiuto validazione Sezione4 {}", e.getMessage(), e); throw new RuntimeException("Errore rifiuto validazione Sezione4", e); }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione4DTO revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Revoca validazione stato Sezione4 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni==null || osservazioni.isBlank()) throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            Sezione4 entity = sezione4Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione4 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) throw new IllegalStateException("Transizione non ammessa: per revocare la validazione la sezione deve essere VALIDATA");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione4 saved = sezione4Repository.save(entity);
            Sezione4DTO dto = sezione4Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_4.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .revocato(Boolean.TRUE).rifiutato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione4DTO response = sezione4Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_4);
            return response;
        } catch (Exception e) { log.error("Errore revoca validazione Sezione4 {}", e.getMessage(), e); throw new RuntimeException("Errore revoca validazione Sezione4", e); }
    }

    @Override
    public Sezione4DTO annullaValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Annulla validazione stato Sezione4 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione4 entity = sezione4Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione4 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per Annullare la validazione la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione4 saved = sezione4Repository.save(entity);
            Sezione4DTO dto = sezione4Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_4.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .rifiutato(Boolean.FALSE).revocato(Boolean.FALSE).annullato(Boolean.TRUE)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione4DTO response = sezione4Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_4);
            return response;
        } catch (Exception e) { log.error("Errore Annulla validazione Sezione4 {}", e.getMessage(), e); throw new RuntimeException("Errore Annulla validazione Sezione4", e); }
    }


}
