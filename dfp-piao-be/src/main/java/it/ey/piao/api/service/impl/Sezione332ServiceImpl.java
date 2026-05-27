package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IObiettiviRisultatiFotografiaService;
import it.ey.piao.api.service.ISezione332Service;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class Sezione332ServiceImpl implements ISezione332Service
{
    private final ISezione332Repository sezione332Repository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final Sezione332Mapper sezione332Mapper;
    private final TipologiaAttivitaMapper tipologiaAttivitaMapper;
    private final AreaTematicaMapper areaTematicaMapper;
    private final AmbitoCompetenzaMapper ambitoCompetenzaMapper;
    private final TipologiaDestinatariMapper tipologiaDestinatariMapper;
    private final ITipologiaAttivitaRepository tipologiaAttivitaRepository;
    private final IAmbitoCompetenzaRepository ambitoCompetenzaRepository;
    private final IAreaTematicaRepository areaTematicaRepository;
    private final ITipologiaDestinatariRepository tipologiaDestinatariRepository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final ITabellaFunzionaleRepository tabellaFunzionaleRepository;
    private final TabellaFunzionaleMapper tabellaFunzionaleMapper;

    private static final Logger log = LoggerFactory.getLogger(Sezione332ServiceImpl.class);
    private final IObiettiviRisultatiFotografiaService obiettiviRisultatiFotografiaService;

    public Sezione332ServiceImpl(ISezione332Repository sezione332Repository,
                                 IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                 Sezione332Mapper sezione332Mapper,
                                 TipologiaAttivitaMapper tipologiaAttivitaMapper,
                                 AreaTematicaMapper areaTematicaMapper,
                                 AmbitoCompetenzaMapper ambitoCompetenzaMapper,
                                 TipologiaDestinatariMapper tipologiaDestinatariMapper,
                                 ITipologiaAttivitaRepository tipologiaAttivitaRepository,
                                 IAmbitoCompetenzaRepository ambitoCompetenzaRepository,
                                 IAreaTematicaRepository areaTematicaRepository,
                                 ITipologiaDestinatariRepository tipologiaDestinatariRepository,
                                 StoricoModificaHelper storicoModificaHelper, ITabellaFunzionaleRepository tabellaFunzionaleRepository, TabellaFunzionaleMapper tabellaFunzionaleMapper,
                                 IObiettiviRisultatiFotografiaService obiettiviRisultatiFotografiaService)
    {
        this.sezione332Repository = sezione332Repository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.sezione332Mapper = sezione332Mapper;
        this.tipologiaAttivitaMapper = tipologiaAttivitaMapper;
        this.areaTematicaMapper = areaTematicaMapper;
        this.ambitoCompetenzaMapper = ambitoCompetenzaMapper;
        this.tipologiaDestinatariMapper = tipologiaDestinatariMapper;
        this.tipologiaAttivitaRepository = tipologiaAttivitaRepository;
        this.ambitoCompetenzaRepository = ambitoCompetenzaRepository;
        this.areaTematicaRepository = areaTematicaRepository;
        this.tipologiaDestinatariRepository = tipologiaDestinatariRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.tabellaFunzionaleRepository = tabellaFunzionaleRepository;
        this.tabellaFunzionaleMapper = tabellaFunzionaleMapper;
        this.obiettiviRisultatiFotografiaService = obiettiviRisultatiFotografiaService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione332DTO getOrCreateSezione(PiaoDTO piaoDTO)
    {
        if (piaoDTO == null || piaoDTO.getId() == null)
        {
            throw new IllegalArgumentException("Il PIAO non può essere nullo e deve avere un ID valido.");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupera Sezione332 dal repository usando idPiao
            Sezione332 existing = sezione332Repository.findByIdPiao(piaoDTO.getId());

            if (existing != null) {
                Sezione332DTO response = sezione332Mapper.toDto(existing,context);
                response.setStatoSezione(
                    StoricoStatoSezioneUtils.getStato(
                        storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_332.name())
                    )
                );

                log.info("Sezione332 trovata per PIAO id={}", piaoDTO.getId());
                return response;
            }

            // Creazione nuova Sezione332 se non esiste
            log.info("Nessuna Sezione332 trovata per PIAO id={}, creazione nuova...", piaoDTO.getId());
            Sezione332 nuova = Sezione332.builder()
                .piao(Piao.builder().id(piaoDTO.getId()).build())
                .build();

            Sezione332 salvata = sezione332Repository.save(nuova);

            StoricoStatoSezione stato = storicoStatoSezioneRepository.save(
                StoricoStatoSezione.builder()
                    .codTipologiaFK(Sezione.SEZIONE_332.name())
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

            Sezione332DTO response = sezione332Mapper.toDto(salvata, context);
            response.setStatoSezione(stato.getStatoSezione().getTesto());

            return response;

        } catch (Exception e) {
            log.error("Errore durante getOrCreateSezione332 per PIAO id={}: {}", piaoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero o la creazione della Sezione332", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Sezione332DTO findByIdPiao(Long idPiao)
    {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Sezione332 existing = sezione332Repository.findByIdPiao(idPiao);
            if (existing != null) {
                // Usa MapStruct per mappare correttamente tutte le relazioni annidate
                Sezione332DTO response = sezione332Mapper.toDto(existing, context);

                // Imposta lo stato corretto
                response.setStatoSezione(
                    StoricoStatoSezioneUtils.getStato(
                        storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.getId(), Sezione.SEZIONE_332.name())
                    )
                );
                response.setTabelleFunzionali(
                    tabellaFunzionaleMapper.toDtoList(
                        tabellaFunzionaleRepository.findByIdEntitaFKAndCodTipologiaFK(
                            existing.getId(),
                            Sezione.SEZIONE_332.name()
                        ),
                        context
                    )
                );

                return response;
            }
            return null;
        } catch (Exception e) {
            log.error("Errore durante findByPiao Sezione332 per PIAO id={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della Sezione332", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdate(Sezione332DTO request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Mappo DTO -> Entity
            Sezione332 entity = sezione332Mapper.toEntity(request, context);

            // Stato
            StatoEnum statoEnum = StatoEnum.fromDescrizione(request.getStatoSezione());
            entity.setIdStato(statoEnum.getId());

            // Salvataggio principale
            Sezione332 savedEntity = sezione332Repository.save(entity);


            if (request.getObiettiviRisultatiFotografia() != null && !request.getObiettiviRisultatiFotografia().isEmpty()) {
                for (ObiettiviRisultatiFotografiaDTO riga : request.getObiettiviRisultatiFotografia()) {
                    riga.setIdSezione332(savedEntity.getId());

                    obiettiviRisultatiFotografiaService.saveOrUpdate(riga);
                }
            }

            // Storico stato
            String statoCorrente = savedEntity.getId() != null
                ? StoricoStatoSezioneUtils.getStato(
                storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getId(), Sezione.SEZIONE_332.name())
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
                        .codTipologiaFK(Sezione.SEZIONE_332.name())
                        .testo(statoEnum.getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build()
                );
            }

            // Salva storico modifica se presente campiModificati
            storicoModificaHelper.salvaStoricoSePresente(request, savedEntity.getId(), request.getIdPiao(), Sezione.SEZIONE_332);

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate Sezione332 per id={}: {}", request.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento della Sezione332", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione332DTO richiediValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Richiesta validazione stato Sezione332 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione332 entity = sezione332Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione332 non trovata"));
            entity.setIdStato(StatoEnum.IN_VALIDAZIONE.getId());
            Sezione332 saved = sezione332Repository.save(entity);
            Sezione332DTO dto = sezione332Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.IN_VALIDAZIONE.getId()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_332.name()).testo(StatoEnum.IN_VALIDAZIONE.getDescrizione())
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione332DTO response = sezione332Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.IN_VALIDAZIONE.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_332);
            return response;
        } catch (Exception e) { log.error("Errore Modifica stato Sezione332 {}", e.getMessage(), e); throw new RuntimeException("Errore Modifica stato Sezione332", e); }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione332DTO validaSezione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Validazione stato Sezione332 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione332 entity = sezione332Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione332 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per validare la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.VALIDATA.getId());
            Sezione332 saved = sezione332Repository.save(entity);
            Sezione332DTO dto = sezione332Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.VALIDATA.getId()).testo(StatoEnum.VALIDATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_332.name()).testo(StatoEnum.VALIDATA.getDescrizione())
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione332DTO response = sezione332Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.VALIDATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_332);
            return response;
        } catch (Exception e) { log.error("Errore validazione Sezione332 {}", e.getMessage(), e); throw new RuntimeException("Errore validazione Sezione332", e); }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione332DTO rifiutaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Rifiuto validazione stato Sezione332 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni == null || osservazioni.isBlank()) throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            Sezione332 entity = sezione332Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione332 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per rifiutare la validazione la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione332 saved = sezione332Repository.save(entity);
            Sezione332DTO dto = sezione332Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_332.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .rifiutato(Boolean.TRUE).revocato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione332DTO response = sezione332Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_332);
            return response;
        } catch (Exception e) { log.error("Errore rifiuto validazione Sezione332 {}", e.getMessage(), e); throw new RuntimeException("Errore rifiuto validazione Sezione332", e); }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione332DTO revocaValidazione(Long id, String osservazioni, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Revoca validazione stato Sezione332 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            if (osservazioni == null || osservazioni.isBlank()) throw new IllegalArgumentException("Le osservazioni devono essere inserite per poter procedere");
            Sezione332 entity = sezione332Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione332 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.VALIDATA.getId())) throw new IllegalStateException("Transizione non ammessa: per revocare la validazione la sezione deve essere VALIDATA");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione332 saved = sezione332Repository.save(entity);
            Sezione332DTO dto = sezione332Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_332.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .revocato(Boolean.TRUE).rifiutato(Boolean.FALSE).annullato(Boolean.FALSE).osservazioni(osservazioni)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione332DTO response = sezione332Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_332);
            return response;
        } catch (Exception e) { log.error("Errore revoca validazione Sezione332 {}", e.getMessage(), e); throw new RuntimeException("Errore revoca validazione Sezione332", e); }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Sezione332DTO annullaValidazione(Long id, String userNameSurname, String userRole, String fiscalCode, String testoSezione, String campiModificati) {
        log.info("Annulla validazione stato Sezione332 per id={}", id);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            Sezione332 entity = sezione332Repository.findById(id).orElseThrow(() -> new RuntimeException("Sezione332 non trovata"));
            if (!Objects.equals(entity.getIdStato(), StatoEnum.IN_VALIDAZIONE.getId())) throw new IllegalStateException("Transizione non ammessa: per annullare la validazione la sezione deve essere IN_VALIDAZIONE");
            entity.setIdStato(StatoEnum.COMPILATA.getId());
            Sezione332 saved = sezione332Repository.save(entity);
            Sezione332DTO dto = sezione332Mapper.toDto(saved, new CycleAvoidingMappingContext());
            storicoStatoSezioneRepository.save(StoricoStatoSezione.builder()
                .statoSezione(StatoSezione.builder().id(StatoEnum.COMPILATA.getId()).testo(StatoEnum.COMPILATA.getDescrizione()).build())
                .idEntitaFK(saved.getId()).codTipologiaFK(Sezione.SEZIONE_332.name()).testo(StatoEnum.COMPILATA.getDescrizione())
                .rifiutato(Boolean.FALSE).revocato(Boolean.FALSE).annullato(Boolean.TRUE)
                .createdBy(fiscalCode).createdByNameSurname(userNameSurname).createdByRole(userRole).build());
            Sezione332DTO response = sezione332Mapper.toDto(saved, context);
            response.setStatoSezione(StatoEnum.COMPILATA.getDescrizione());
            if(campiModificati != null && !campiModificati.isBlank() && testoSezione != null && !testoSezione.isBlank()){ dto.setCampiModificati(campiModificati); dto.setTestoSezione(testoSezione); }
            storicoModificaHelper.salvaStoricoSePresente(dto, saved.getId(), saved.getPiao().getId(), Sezione.SEZIONE_332);
            return response;
        } catch (Exception e) { log.error("Errore annulla validazione Sezione332 {}", e.getMessage(), e); throw new RuntimeException("Errore annulla validazione Sezione332", e); }
    }

    @Override
    public void saveMongoData(Sezione332DTO request) {}

    @Override
    public Sezione332DTO loadMongoData(Sezione332DTO sezione) { return null; }

    @Override
    @Transactional(readOnly = true)
    public List<TipologiaAttivitaDTO> getTipologiaAttivita()
    {
        List<TipologiaAttivitaDTO> response = null;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try{
            response = tipologiaAttivitaMapper.toDtoList(tipologiaAttivitaRepository.findAll(), context);

            if(response.isEmpty())
            {
                log.warn("Tentativo di recupero di TipologiaAttivita fallito");
                throw new RuntimeException("Tentativo di recupero di TipologiaAttivita fallito");
            }

        } catch (Exception e) {
            log.error("Errore durante il recupero di TipologiaAttivita {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero di TipologiaAttivita", e);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmbitoCompetenzaDTO> getAmbitoCompetenza()
    {
        List<AmbitoCompetenzaDTO> response = null;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try{
            response = ambitoCompetenzaMapper.toDtoList(ambitoCompetenzaRepository.findAll(), context);

            if(response.isEmpty())
            {
                log.warn("Tentativo di recupero di AmbitoCompetenza fallito");
                throw new RuntimeException("Tentativo di recupero di AmbitoCompetenza fallito");
            }

        } catch (Exception e) {
            log.error("Errore durante il recupero di AmbitoCompetenza {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero di AmbitoCompetenza", e);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaTematicaDTO> getAreaTematica()
    {
        List<AreaTematicaDTO> response = null;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try{
            response = areaTematicaMapper.toDtoList(areaTematicaRepository.findAll(), context);

            if(response.isEmpty())
            {
                log.warn("Tentativo di recupero di AreaTematica fallito");
                throw new RuntimeException("Tentativo di recupero di AreaTematica fallito");
            }

        } catch (Exception e) {
            log.error("Errore durante il recupero di AreaTematica {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero di AreaTematica", e);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipologiaDestinatariDTO> getTipologiaDestinatari()
    {
        List<TipologiaDestinatariDTO> response = null;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try{
            response = tipologiaDestinatariMapper.toDtoList(tipologiaDestinatariRepository.findAll(), context);

            if(response.isEmpty())
            {
                log.warn("Tentativo di recupero di TipologiaDestinatari fallito");
                throw new RuntimeException("Tentativo di recupero le TipologiaDestinatari fallito");
            }

        } catch (Exception e) {
            log.error("Errore durante il recupero di TipologiaDestinatari {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero di TipologiaDestinatari", e);
        }

        return response;
    }
}
