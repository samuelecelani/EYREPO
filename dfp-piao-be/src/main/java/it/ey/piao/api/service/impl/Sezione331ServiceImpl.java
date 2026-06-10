package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.PiaoMapper;
import it.ey.piao.api.mapper.Sezione331Mapper;
import it.ey.piao.api.mapper.TabellaFunzionaleMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.ISezione331Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.ITabellaFunzionaleRepository;
import it.ey.piao.api.service.ISezione331Service;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
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
public class Sezione331ServiceImpl implements ISezione331Service {
    private final ApplicationEventPublisher eventPublisher;
    private final ISezione331Repository sezione331Repository;
    private final Sezione331Mapper sezione331Mapper;
    private static final Logger log = LoggerFactory.getLogger(Sezione331ServiceImpl.class);
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final PiaoMapper piaoMapper;
    private final StoricoModificaHelper storicoModificaHelper;
    private final ITabellaFunzionaleRepository tabellaFunzionaleRepository;
    private final TabellaFunzionaleMapper tabellaFunzionaleMapper;

    public Sezione331ServiceImpl(ISezione331Repository sezione331Repository,
                                 Sezione331Mapper sezione331Mapper,
                                 ApplicationEventPublisher eventPublisher,
                                 IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                 PiaoMapper piaoMapper,
                                 StoricoModificaHelper storicoModificaHelper, ITabellaFunzionaleRepository tabellaFunzionaleRepository, TabellaFunzionaleMapper tabellaFunzionaleMapper) {
        this.sezione331Repository = sezione331Repository;
        this.sezione331Mapper = sezione331Mapper;
        this.eventPublisher = eventPublisher;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.piaoMapper = piaoMapper;
        this.storicoModificaHelper = storicoModificaHelper;
        this.tabellaFunzionaleRepository = tabellaFunzionaleRepository;
        this.tabellaFunzionaleMapper = tabellaFunzionaleMapper;
    }



    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione331DTO getOrCreateSezione(PiaoDTO piaoDTO) {
        if (piaoDTO == null || piaoDTO.getId() == null) {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();


        try {
            // Recupera Sezione331 dal repository usando idPiao
            Sezione331 existing = sezione331Repository.findByIdPiao(piaoDTO.getId());

            if (existing != null) {
                Sezione331DTO response = sezione331Mapper.toDto(existing,context);
                response.setStatoSezione(
                    StoricoStatoSezioneUtils.getStato(
                        storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_331.name())
                    )
                );
                log.info("Sezione331 trovata per PIAO id={}", piaoDTO.getId());
                return response;
            }

            // Creazione nuova Sezione331 se non esiste
            log.info("Nessuna Sezione331 trovata per PIAO id={}, creazione nuova...", piaoDTO.getId());
            Sezione331 nuova = Sezione331.builder()
                .piao(Piao.builder().id(piaoDTO.getId()).build())
                .build();

            Sezione331 salvata = sezione331Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .codTipologiaFK(Sezione.SEZIONE_331.name())
                    .statoSezione(StatoSezione.builder()
                        .id(StatoEnum.DA_COMPILARE.getId())
                        .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                        .build())
                    .idEntitaFK(salvata.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .createdByNameSurname(piaoDTO.getCreatedByNameSurname())
                    .createdByRole(piaoDTO.getCreatedByRole())
                    .build()
            );

            Sezione331DTO response = sezione331Mapper.toDto(salvata, context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione331 per PIAO id={}: {}", piaoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione331", e);
        }
    }







    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveOrUpdate(Sezione331DTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Mappo DTO -> Entity
            Sezione331 entity = sezione331Mapper.toEntity(request, context);

            // Evento rollback in caso di update
            if (entity.getId() != null) {
                sezione331Repository.findById(entity.getId())
                    .ifPresent(existing -> eventPublisher.publishEvent(new BeforeUpdateEvent<>(Sezione331.class, existing)));
            }

            // Stato
            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());

            // Salvataggio principale
            Sezione331 savedEntity = sezione331Repository.save(entity);

            // Storico stato
            String statoCorrente = savedEntity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getId(), Sezione.SEZIONE_331.name())
            )
                : null;

            if (!statoEnum.name().equals(statoCorrente)) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder()
                        .statoSezione(StatoSezione.builder()
                            .id(statoEnum.getId())
                            .testo(statoEnum.getDescrizione())
                            .build())
                        .idEntitaFK(savedEntity.getId())
                        .codTipologiaFK(Sezione.SEZIONE_331.name())
                        .testo(statoEnum.getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build()
                );
            }

            // MapStruct per evento
            Sezione331DTO savedDto = sezione331Mapper.toDto(savedEntity, context);

            // Salva storico modifica se presente campiModificati
            storicoModificaHelper.salvaStoricoSePresente(request, savedEntity.getId(), request.getIdPiao(), Sezione.SEZIONE_331);

            // Evento successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedDto));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione331 per id={}: {}", request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(sezione331Mapper.toEntity(request, context), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione331", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione331DTO richiediValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Richiesta validazione stato Sezione331 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione331 entity = sezione331Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione331 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione331 saved = sezione331Repository.save(entity);
            Sezione331DTO dto = sezione331Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.IN_VALIDAZIONE.getId()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_331.name()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione331DTO response = sezione331Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_331);
            return response;
        } catch (Exception e) { log.error("Errore Modifica stato Sezione331 {}", e.getMessage(), e); throw new RuntimeException("Errore Modifica stato Sezione331", e); }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione331DTO validaSezione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Validazione stato Sezione331 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione331 entity = sezione331Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione331 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per validare la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.VALIDATA.getId());
            Sezione331 saved = sezione331Repository.save(entity);
            Sezione331DTO dto = sezione331Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.VALIDATA.getId()).testo(StatoEnum.VALIDATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_331.name()).testo(StatoEnum.VALIDATA.getDescrizione())
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione331DTO response = sezione331Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.VALIDATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_331);
            return response;
        } catch (Exception e) { log.error("Errore validazione Sezione331 {}", e.getMessage(), e); throw new RuntimeException("Errore validazione Sezione331", e); }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione331DTO rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Rifiuto validazione stato Sezione331 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni==null || osservazioni.isBlank()) throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            Sezione331 entity = sezione331Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione331 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per rifiutare la validazione la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione331 saved = sezione331Repository.save(entity);
            Sezione331DTO dto = sezione331Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_331.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .rifiutato(Boolean.TRUE).revocato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione331DTO response = sezione331Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_331);
            return response;
        } catch (Exception e) { log.error("Errore rifiuto validazione Sezione331 {}", e.getMessage(), e); throw new RuntimeException("Errore rifiuto validazione Sezione331", e); }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione331DTO revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Revoca validazione stato Sezione331 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni==null || osservazioni.isBlank()) throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            Sezione331 entity = sezione331Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione331 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) throw new IllegalStateException("Transizione non ammessa: per revocare la validazione la sezione deve essere VALIDATA");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione331 saved = sezione331Repository.save(entity);
            Sezione331DTO dto = sezione331Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_331.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .revocato(Boolean.TRUE).rifiutato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione331DTO response = sezione331Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_331);
            return response;
        } catch (Exception e) { log.error("Errore revoca validazione Sezione331 {}", e.getMessage(), e); throw new RuntimeException("Errore revoca validazione Sezione331", e); }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Sezione331DTO annullaValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Annulla validazione stato Sezione331 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione331 entity = sezione331Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione331 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per annullare la validazione la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione331 saved = sezione331Repository.save(entity);
            Sezione331DTO dto = sezione331Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_331.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .rifiutato(Boolean.FALSE).revocato(Boolean.FALSE).annullato(Boolean.TRUE)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione331DTO response = sezione331Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_331);
            return response;
        } catch (Exception e) { log.error("Errore annulla validazione Sezione331 {}", e.getMessage(), e); throw new RuntimeException("Errore annulla validazione Sezione331", e); }
    }


    @Transactional(readOnly = true)
    @Override
    public Sezione331DTO findByIdPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione331 existing = sezione331Repository.findByIdPiao(idPiao);
            if (existing != null) {
                // Usa MapStruct per mappare correttamente tutte le relazioni annidate
                Sezione331DTO response = sezione331Mapper.toDto(existing, context);

                // Imposta lo stato corretto
                response.setStatoSezione(
                    StoricoStatoSezioneUtils.getStato(
                        storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_331.name())
                    )
                );

                response.setTabelleFunzionali(
                    tabellaFunzionaleMapper.toDtoList(
                        tabellaFunzionaleRepository.findByIdEntitaFKAndCodTipologiaFK(
                            existing.getId(),
                            Sezione.SEZIONE_331.name()
                        ),
                        context
                    )
                );


                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione331 per PIAO id={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione331", e);
        }
    }

    @Override
    public void saveMongoData(Sezione331DTO request) { }

    @Override
    public Sezione331DTO loadMongoData(Sezione331DTO sezione) { return null; }
}
