package it.ey.piao.api.service.impl;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione32DTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.Sezione32Mapper;
import it.ey.piao.api.mapper.TabellaFunzionaleMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.ISezione32Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.ITabellaFunzionaleRepository;
import it.ey.piao.api.service.ISezione32Service;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class Sezione32ServiceImpl implements ISezione32Service
{
    private final ISezione32Repository sezione32Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final ITabellaFunzionaleRepository tabellaFunzionaleRepository;
    private final Sezione32Mapper sezione32Mapper;
    private final TabellaFunzionaleMapper tabellaFunzionaleMapper;
    private final StoricoModificaHelper storicoModificaHelper;

    private static final Logger log = LoggerFactory.getLogger(Sezione32ServiceImpl.class);

    public Sezione32ServiceImpl(ISezione32Repository sezione32Repository,
                                IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                ITabellaFunzionaleRepository tabellaFunzionaleRepository,
                                Sezione32Mapper sezione32Mapper,
                                TabellaFunzionaleMapper tabellaFunzionaleMapper,
                                StoricoModificaHelper storicoModificaHelper)
    {
        this.sezione32Repository = sezione32Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.tabellaFunzionaleRepository = tabellaFunzionaleRepository;
        this.sezione32Mapper = sezione32Mapper;
        this.tabellaFunzionaleMapper = tabellaFunzionaleMapper;
        this.storicoModificaHelper = storicoModificaHelper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione32DTO getOrCreateSezione(PiaoDTO piao)
    {
        if (piao == null || piao.getId() == null)
        {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione32 dal repository usando il Piao
            Sezione32 existing = sezione32Repository.findByIdPiao(piao.getId());

            if (existing != null)
            {
                // USA MAPSTRUCT per mappare correttamente tutte le relazioni annidate
                Sezione32DTO response = sezione32Mapper.toDto(existing, context);

                log.info("Sezione32 trovata per PIAO id={}", piao.getId());
                return response;
            }

            // Creazione nuova Sezione32 se non esiste
            log.info("Nessuna Sezione32 trovata per PIAO id={}, creazione nuova...", piao.getId());
            Sezione32 nuova = Sezione32.builder()
                .piao(Piao.builder().id(piao.getId()).build())
                .build();

            Sezione32 salvata = sezione32Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .codTipologiaFK(Sezione.SEZIONE_32.name())
                .statoSezione(StatoSezione.builder()
                    .id(StatoEnum.DA_COMPILARE.getId())
                    .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                    .build())
                .idEntitaFK(salvata.getId())
                .testo(StatoEnum.DA_COMPILARE.getDescrizione())
                .createdByNameSurname(piao.getCreatedByNameSurname())
                .createdByRole(piao.getCreatedByRole())
                .build());

            Sezione32DTO response = sezione32Mapper.toDto(salvata, context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione32 per PIAO id={}: {}", piao.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione32", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione32DTO findByIdPiao(Long idPiao)
    {
        if (idPiao == null)
        {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione32 existing = sezione32Repository.findByIdPiao(idPiao);
            if (existing != null)
            {
                // USA MAPSTRUCT
                Sezione32DTO response = sezione32Mapper.toDto(existing, context);
                response.setStatoSezione(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_32.name())));

                // Recupero tabelle funzionali associate alla sezione 32
                response.setTabelleFunzionali(tabellaFunzionaleMapper.toDtoList(
                    tabellaFunzionaleRepository.findByIdEntitaFKAndCodTipologiaFK(existing.getId(), Sezione.SEZIONE_32.name()),
                    context
                ));

                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione32 per PIAO id={}: {}",idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione32", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(Sezione32DTO request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione32 entity = sezione32Mapper.toEntity(request, context);

            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());

            Sezione32 savedEntity = sezione32Repository.save(entity);

            // Gestione storico stato: evita duplicazioni se lo stato non cambia
            String statoCorrenteStorico = entity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                    entity.getId(),
                    Sezione.SEZIONE_32.name()
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
                        .codTipologiaFK(Sezione.SEZIONE_32.name())
                        .testo(statoEnum.getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build()
                );
            }

            // Salva storico modifica se presente campiModificati
            storicoModificaHelper.salvaStoricoSePresente(request, savedEntity.getId(), request.getIdPiao(), Sezione.SEZIONE_32);

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione32 per id={}: {}",
                request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione32", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione32DTO richiediValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati)
    {
        log.info("Richiesta validazione stato Sezione32 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione32 entity = sezione32Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione32 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione32 saved = sezione32Repository.save(entity);
            Sezione32DTO dto = sezione32Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder().id(StatoEnum.IN_VALIDAZIONE.getId()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione()).build())
                    .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_32.name()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                    .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build()
            );
            Sezione32DTO response = sezione32Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_32);
            return response;
        } catch (Exception e) {
            log.error("Errore Modifica stato Sezione32 {}", e.getMessage(), e);
            throw new RuntimeException("Errore Modifica stato Sezione32", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione32DTO validaSezione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati)
    {
        log.info("Validazione stato Sezione32 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione32 entity = sezione32Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione32 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per validare la sezione deve essere IN_VALIDAZIONE");
            }
            entity.setIdStato(StatoEnum.VALIDATA.getId());
            Sezione32 saved = sezione32Repository.save(entity);
            Sezione32DTO dto = sezione32Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder().id(StatoEnum.VALIDATA.getId()).testo(StatoEnum.VALIDATA.getDescrizione()).build())
                    .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_32.name()).testo(StatoEnum.VALIDATA.getDescrizione())
                    .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build()
            );
            Sezione32DTO response = sezione32Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.VALIDATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_32);
            return response;
        } catch (Exception e) {
            log.error("Errore validazione Sezione32 {}", e.getMessage(), e);
            throw new RuntimeException("Errore validazione Sezione32", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione32DTO rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati)
    {
        log.info("Rifiuto validazione stato Sezione32 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni == null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            Sezione32 entity = sezione32Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione32 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per rifiutare la validazione la sezione deve essere IN_VALIDAZIONE");
            }
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione32 saved = sezione32Repository.save(entity);
            Sezione32DTO dto = sezione32Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                    .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_32.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                    .rifiutato(Boolean.TRUE).revocato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                    .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build()
            );
            Sezione32DTO response = sezione32Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_32);
            return response;
        } catch (Exception e) {
            log.error("Errore rifiuto validazione Sezione32 {}", e.getMessage(), e);
            throw new RuntimeException("Errore rifiuto validazione Sezione32", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione32DTO revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati)
    {
        log.info("Revoca validazione stato Sezione32 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni == null || osservazioni.isBlank()){
                throw  new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            }
            Sezione32 entity = sezione32Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione32 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per revocare la validazione la sezione deve essere VALIDATA");
            }
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione32 saved = sezione32Repository.save(entity);
            Sezione32DTO dto = sezione32Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                    .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_32.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                    .revocato(Boolean.TRUE).rifiutato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                    .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build()
            );
            Sezione32DTO response = sezione32Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_32);
            return response;
        } catch (Exception e) {
            log.error("Errore revoca validazione Sezione32 {}", e.getMessage(), e);
            throw new RuntimeException("Errore revoca validazione Sezione32", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione32DTO annullaValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati)
    {
        log.info("Annulla validazione stato Sezione32 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione32 entity = sezione32Repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sezione32 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) {
                throw new IllegalStateException("Transizione non ammessa: per annullare la validazione la sezione deve essere IN_VALIDAZIONE");
            }
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione32 saved = sezione32Repository.save(entity);
            Sezione32DTO dto = sezione32Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                    .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_32.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                    .rifiutato(Boolean.FALSE).revocato(Boolean.FALSE).annullato(Boolean.TRUE)
                    .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build()
            );
            Sezione32DTO response = sezione32Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){
                dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione);
            }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_32);
            return response;

        } catch (Exception e) {
            log.error("Errore annulla validazione Sezione32 {}", e.getMessage(), e);
            throw new RuntimeException("Errore annulla validazione Sezione32", e);
        }
    }

    @Override
    public void saveMongoData(Sezione32DTO request) { }

    @Override
    public Sezione32DTO loadMongoData(Sezione32DTO sezione) { return null; }
}
