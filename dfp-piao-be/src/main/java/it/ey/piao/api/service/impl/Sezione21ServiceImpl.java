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
import it.ey.piao.api.service.IOVPRisorsaFinanziariaService;
import it.ey.piao.api.service.IOVPService;
import it.ey.piao.api.service.IOVPStrategiaService;
import it.ey.piao.api.service.ISezione21Service;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.SezioneUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final IOVPStrategiaService ovpStrategiaService;
    private final AllegatoRepository allegatoRepository;
    private final IOVPRisorsaFinanziariaService ovpRisorsaFinanziariaService;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoUtils mongoUtil;
    private final IOVPService ovpService;
    private final StoricoModificaHelper storicoModificaHelper;

    private static final Logger log = LoggerFactory.getLogger(Sezione21ServiceImpl.class);

    public Sezione21ServiceImpl(ISezione21Repository sezione21Repository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, CommonMapper commonMapper, Sezione21Mapper sezione21Mapper, PiaoMapper piaoMapper, OVPMapper ovpMapper, ISwotPuntiForzaRepository swotPuntiForzaRepository, ISwotPuntiDebolezzaRepository swotPuntiDebolezzaRepository, ISwotOpportunitaRepository swotOpportunitaRepository, ISwotMinacceRepository swotMinacceRepository, IUlterioriInfoRepository ulterioriInfoRepository, IOVPStrategiaService ovpStrategiaService, AllegatoRepository allegatoRepository, IOVPRisorsaFinanziariaService ovpRisorsaFinanziariaService, ApplicationEventPublisher eventPublisher, MongoUtils mongoUtil, IOVPService ovpService, StoricoModificaHelper storicoModificaHelper) {
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
        this.ovpStrategiaService = ovpStrategiaService;
        this.allegatoRepository = allegatoRepository;
        this.ovpRisorsaFinanziariaService = ovpRisorsaFinanziariaService;
        this.eventPublisher = eventPublisher;
        this.mongoUtil = mongoUtil;
        this.ovpService = ovpService;
        this.storicoModificaHelper = storicoModificaHelper;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione21DTO getOrCreateSezione(PiaoDTO piao) {
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
                response = loadMongoData(response);

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
                .createdByNameSurname(piao.getCreatedByNameSurname())
                .createdByRole(piao.getCreatedByRole())
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
    public Sezione21DTO loadMongoData(Sezione21DTO sezione21) {
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
    public void saveOrUpdate(Sezione21DTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

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
                        .codTipologiaFK(Sezione.SEZIONE_21.name())
                        .testo(statoEnum.getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build()
                );
            }

            if (request.getOvp() != null && !request.getOvp().isEmpty()) {
                request.getOvp()
                    .forEach(ovp -> {
                        ovpRisorsaFinanziariaService.saveOrUpdate(ovp.getRisorseFinanziarie(), ovp.getId());


                        if (ovp.getId() != null && ovp.getValoreIndice() != null && StringUtils.isNotBlank(ovp.getDescrizioneIndice())) {
                            ovpService.updateValoreIndiceAndDescrizione(
                                ovp.getId(),
                                ovp.getValoreIndice(),
                                ovp.getDescrizioneIndice());
                        }
                    });
            }

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
            storicoModificaHelper.salvaStoricoSePresente(request, savedEntity.getId(), request.getIdPiao(), Sezione.SEZIONE_21);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedEntity));
        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione21 per id={}: {}", request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(sezione21Mapper.toEntity(request, context), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione21", e);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 10)
    public Sezione21DTO richiediValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Richiesta validazione stato sezione21 per id={}", id);

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione21 entity = sezione21Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("sezione21 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione21 saved = sezione21Repository.save(entity);
            Sezione21DTO dto = sezione21Mapper.toDto(saved, new CycleAvoidingMappingContext());

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
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione21DTO response = sezione21Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_21);
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato sezione21 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato sezione21", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione21DTO validaSezione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Validazione stato Sezione21 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupero la sezione da DB
            Sezione21 entity = sezione21Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione21 non trovata"));

            //  si valida solo se in validazione
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per validare la sezione deve essere IN_VALIDAZIONE");
            }

            // aggiorniamo lostato a validata
            entity.setIdStato(StatoEnum.VALIDATA.getId());
            // salviamo su DB la sezione con il nuovo stato
            Sezione21 saved = sezione21Repository.save(entity);
            Sezione21DTO dto = sezione21Mapper.toDto(saved, new CycleAvoidingMappingContext());

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
                    .codTipologiaFK(Sezione.SEZIONE_21.name())
                    .testo(StatoEnum.VALIDATA.getDescrizione())
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione21DTO response = sezione21Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.VALIDATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_21);
            return response;

        } catch (Exception e) {
            log.error("Errore validazione Sezione21 {}", e.getMessage(), e);
            throw new RuntimeException("Errore validazione Sezione21", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione21DTO rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Rifiuto validazione stato Sezione21 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            if (osservazioni ==null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            //  Recupero la sezione da DB
            Sezione21 entity = sezione21Repository.findById(id)
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
            Sezione21 saved = sezione21Repository.save(entity);
            Sezione21DTO dto = sezione21Mapper.toDto(saved, new CycleAvoidingMappingContext());

            //  Inserisco una nuova riga nello storico:
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_21.name())
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

            Sezione21DTO response = sezione21Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_21);

            return response;

        } catch (Exception e) {
            log.error("Errore rifiuto validazione Sezione21 {}", e.getMessage(), e);
            throw new RuntimeException("Errore rifiuto validazione Sezione21", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione21DTO revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Revoca validazione stato Sezione21 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            if (osservazioni ==null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            // Recupero la sezione da DB
            Sezione21 entity = sezione21Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione21 non trovata"));

            //  possiamo revocare SOLO se lo stato corrente è VALIDATA
            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) {
                throw new IllegalStateException(
                    "Transizione non ammessa: per revocare la validazione la sezione deve essere VALIDATA"
                );
            }

            //  Cambio stato: VALIDATA -> COMPILATA (revoca della validazione)
            entity.setIdStato(StatoEnum.COMPILATA.getId());

            //  Persisto la modifica su DB
            Sezione21 saved = sezione21Repository.save(entity);
            Sezione21DTO dto = sezione21Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_21.name())
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

            Sezione21DTO response = sezione21Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_21);

            return response;

        } catch (Exception e) {
            log.error("Errore revoca validazione Sezione21 {}", e.getMessage(), e);
            throw new RuntimeException("Errore revoca validazione Sezione21", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione21DTO annullaValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Annulla validazione stato Sezione21 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            //  Recupero la sezione da DB
            Sezione21 entity = sezione21Repository.findById(id)
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
            Sezione21 saved = sezione21Repository.save(entity);
            Sezione21DTO dto = sezione21Mapper.toDto(saved, new CycleAvoidingMappingContext());

            //  Inserisco una nuova riga nello storico:
            // questa avrà lo stato COMPILATO e il flag annullato a "TRUE"
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(
                        StatoSezione.builder()
                            .id(StatoEnum.COMPILATA.getId())
                            .testo(StatoEnum.COMPILATA.getDescrizione())
                            .build()
                    )
                    .idEntitaFK(saved.getId())
                    .codTipologiaFK(Sezione.SEZIONE_21.name())
                    .testo(StatoEnum.COMPILATA.getDescrizione())
                    .rifiutato(Boolean.FALSE)
                    .revocato(Boolean.FALSE)
                    .annullato(Boolean.TRUE)
                    .createdBy(fiscalCode)
                    .createdByNameSurname(userNameSurname)
                    .createdByRole(userRole)
                    .build()
            );

            Sezione21DTO response = sezione21Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati);
                dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_21);

            return response;

        } catch (Exception e) {
            log.error("Errore annulla validazione Sezione21 {}", e.getMessage(), e);
            throw new RuntimeException("Errore annulla  validazione Sezione21", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoData(Sezione21DTO request) {
        if (request == null || request.getId() == null) {
            log.warn("Sezione21Id o Request è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva o soft-delete SwotMinacce MongoDB
            SwotMinacce swotMinacceEntity = request.getSwotMinacce() != null
                ? commonMapper.swotMinacceDtoToEntity(request.getSwotMinacce(), context)
                : null;
            if (swotMinacceEntity != null) swotMinacceEntity.setExternalId(request.getId());
            mongoUtil.saveItem(swotMinacceEntity, request.getId(),
                swotMinacceRepository, SwotMinacce.class);

            // Salva o soft-delete SwotOpportunita MongoDB
            SwotOpportunita swotOpportunitaEntity = request.getSwotOpportunita() != null
                ? commonMapper.swotOpportunitaDtoToEntity(request.getSwotOpportunita(), context)
                : null;
            if (swotOpportunitaEntity != null) swotOpportunitaEntity.setExternalId(request.getId());
            mongoUtil.saveItem(swotOpportunitaEntity, request.getId(),
                swotOpportunitaRepository, SwotOpportunita.class);

            // Salva o soft-delete SwotPuntiDebolezza MongoDB
            SwotPuntiDebolezza swotPuntiDebolezzaEntity = request.getSwotPuntiDebolezza() != null
                ? commonMapper.swotPuntiDebolezzaDtoToEntity(request.getSwotPuntiDebolezza(), context)
                : null;
            if (swotPuntiDebolezzaEntity != null) swotPuntiDebolezzaEntity.setExternalId(request.getId());
            mongoUtil.saveItem(swotPuntiDebolezzaEntity, request.getId(),
                swotPuntiDebolezzaRepository, SwotPuntiDebolezza.class);

            // Salva o soft-delete SwotPuntiForza MongoDB
            SwotPuntiForza swotPuntiForzaEntity = request.getSwotPuntiForza() != null
                ? commonMapper.swotPuntiForzaDtoToEntity(request.getSwotPuntiForza(), context)
                : null;
            if (swotPuntiForzaEntity != null) swotPuntiForzaEntity.setExternalId(request.getId());
            mongoUtil.saveItem(swotPuntiForzaEntity, request.getId(),
                swotPuntiForzaRepository, SwotPuntiForza.class);

            // Salva o soft-delete UlterioriInfo MongoDB
            UlterioriInfo ulterioriInfoEntity = request.getUlterioriInfo() != null
                ? commonMapper.ulterioriInfoDtoToEntity(request.getUlterioriInfo(), context)
                : null;
            if (ulterioriInfoEntity != null) ulterioriInfoEntity.setExternalId(request.getId());
            mongoUtil.saveItem(ulterioriInfoEntity, request.getId(),
                ulterioriInfoRepository, UlterioriInfo.class,
                en -> en.setTipoSezione(Sezione.SEZIONE_21),
                "tipoSezione", Sezione.SEZIONE_21);

            log.debug("Dati MongoDB salvati per Sezione21 id={}", request.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Sezione21 id={}: {}", request.getId(), e.getMessage(), e);
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

            Sezione21DTO response = sezione21Mapper.toDto(sezione21, context);

            //Recupero lo stato della singola sezione
            response.setStatoSezione(StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(sezione21.getId(), Sezione.SEZIONE_21.name())
            ));

            // Carica SOLO i dati MongoDB della sezione21
            response = loadMongoData(response);

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
