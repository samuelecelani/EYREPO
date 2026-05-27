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
import it.ey.piao.api.service.helper.StoricoModificaHelper;
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
import java.util.Objects;
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
    private final StoricoModificaHelper storicoModificaHelper;

    private static final Logger log = LoggerFactory.getLogger(Sezione22ServiceImpl.class);

    public  Sezione22ServiceImpl(ISezione22Repository sezione22Repository, IUlterioriInfoRepository ulterioriInfoRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, ApplicationEventPublisher eventPublisher, PiaoMapper piaoMapper, Sezione22Mapper sezione22Mapper, CommonMapper commonMapper, MongoUtils mongoUtil, IAzioneRepository azioneRepository, IObbiettivoPerformanceService obbiettivoPerformanceService, IFaseService faseService, IAdempimentoService adempimentoService, AllegatoRepository allegatoRepository, StoricoModificaHelper storicoModificaHelper) {
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
        this.storicoModificaHelper = storicoModificaHelper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione22DTO getOrCreateSezione(PiaoDTO piao) {

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
                response = loadMongoData(response);

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
                .createdByNameSurname(piao.getCreatedByNameSurname())
                .createdByRole(piao.getCreatedByRole())
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
    public Sezione22DTO loadMongoData(Sezione22DTO sezione22) {
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
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build()
                );
            }

            Sezione22DTO response = sezione22Mapper.toDto(savedEntity, context);

            //Allegati
            if (request.getAllegati() != null && !request.getAllegati().isEmpty()) {
                List<Allegato> allegati = request.getAllegati().stream()
                    .filter(a -> a.getId() != null && allegatoRepository.existsById(a.getId()))
                    .map(dto -> commonMapper.allegatoDtoToEntity(dto, context)).toList();

                allegatoRepository.saveAll(allegati);
            }

            // Salva i dati MongoDB tramite metodo dedicato
            saveMongoData(request);

            // Salva storico modifica se presente campiModificati
            storicoModificaHelper.salvaStoricoSePresente(request, savedEntity.getId(), request.getIdPiao(), Sezione.SEZIONE_22);

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
    public Sezione22DTO richiediValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Richiesta validazione stato Sezione22 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione22 entity = sezione22Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione22 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione22 saved = sezione22Repository.save(entity);
            Sezione22DTO dto = sezione22Mapper.toDto(saved, new CycleAvoidingMappingContext());

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
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione22DTO response = sezione22Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_22);
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione22 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione22", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione22DTO validaSezione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Validazione stato Sezione22 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupero la sezione da DB
            Sezione22 entity = sezione22Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione22 non trovata"));

            //  si valida solo se in validazione
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per validare la sezione deve essere IN_VALIDAZIONE");
            }

            // aggiorniamo lostato a validata
            entity.setIdStato(StatoEnum.VALIDATA.getId());
            // salviamo su DB la sezione con il nuovo stato
            Sezione22 saved = sezione22Repository.save(entity);
            Sezione22DTO dto = sezione22Mapper.toDto(saved, new CycleAvoidingMappingContext());

            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.VALIDATA.getId())
                            .testo(StatoEnum.VALIDATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_22.name())
                    .testo(StatoEnum.VALIDATA.getDescrizione())
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione22DTO response = sezione22Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.VALIDATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_22);
            return response;

        } catch (Exception e) {
            log.error("Errore validazione Sezione22 {}", e.getMessage(), e);
            throw new RuntimeException("Errore validazione Sezione22", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione22DTO rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Rifiuto validazione stato Sezione22 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            if (osservazioni ==null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            //  Recupero la sezione da DB
            Sezione22  entity = sezione22Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione22 non trovata"));

            //  si può rifiutare SOLO se la sezione è IN_VALIDAZIONE
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per rifiutare la validazione la sezione deve essere IN_VALIDAZIONE"
                );
            }

            // Aggiorno lo stato della sezione a COMPILATA ,quindi si ritorna al vecchio stato
            entity.setIdStato(StatoEnum.COMPILATA.getId());

            // salviamo su DB la sezione con il nuovo stato
            Sezione22  saved = sezione22Repository.save(entity);
            Sezione22DTO dto = sezione22Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_22.name())
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

            Sezione22DTO response = sezione22Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_22);

            return response;

        } catch (Exception e) {
            log.error("Errore rifiuto validazione Sezione22  {}", e.getMessage(), e);
            throw new RuntimeException("Errore rifiuto validazione Sezione22 ", e);
        }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione22DTO revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Revoca validazione stato Sezione22 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            if (osservazioni ==null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            // Recupero la sezione da DB
            Sezione22 entity = sezione22Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione22 non trovata"));

            //  possiamo revocare SOLO se lo stato corrente è VALIDATA
            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per revocare la validazione la sezione deve essere VALIDATA"
                );
            }

            //  Cambio stato: VALIDATA -> COMPILATA (revoca della validazione)
            entity.setIdStato(StatoEnum.COMPILATA.getId());

            //  Persisto la modifica su DB
            Sezione22 saved = sezione22Repository.save(entity);
            Sezione22DTO dto = sezione22Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_22.name())
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

            Sezione22DTO response = sezione22Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_22);

            return response;

        } catch (Exception e) {
            log.error("Errore revoca validazione Sezione22 {}", e.getMessage(), e);
            throw new RuntimeException("Errore revoca validazione Sezione22", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione22DTO annullaValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Annulla validazione stato Sezione22 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            //  Recupero la sezione da DB
            Sezione22  entity = sezione22Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione22 non trovata"));

            //  si può annullare SOLO se la sezione è IN_VALIDAZIONE
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per annullare la validazione la sezione deve essere IN_VALIDAZIONE"
                );
            }

            // Aggiorno lo stato della sezione a COMPILATA ,quindi si ritorna al vecchio stato
            entity.setIdStato(StatoEnum.COMPILATA.getId());

            // salviamo su DB la sezione con il nuovo stato
            Sezione22  saved = sezione22Repository.save(entity);
            Sezione22DTO dto = sezione22Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_22 .name())
                    .testo(StatoEnum.COMPILATA.getDescrizione())
                    .rifiutato(Boolean.FALSE)
                    .revocato(Boolean.FALSE)
                    .annullato(Boolean.TRUE)
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione22DTO response = sezione22Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_22);

            return response;

        } catch (Exception e) {
            log.error("Errore annulla validazione Sezione22  {}", e.getMessage(), e);
            throw new RuntimeException("Errore annulla validazione Sezione22 ", e);
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
            response = loadMongoData(response);

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
    public void saveMongoData(Sezione22DTO request) {
        if (request == null || request.getId() == null) {
            log.warn("Request o ID è null, skip salvataggio MongoDB");
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
                en -> en.setTipoSezione(Sezione.SEZIONE_22),
                "tipoSezione", Sezione.SEZIONE_22);

            log.debug("Dati MongoDB salvati per Sezione22 id={}", request.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione22 id={}: {}", request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Sezione22", e);
        }
    }
}

