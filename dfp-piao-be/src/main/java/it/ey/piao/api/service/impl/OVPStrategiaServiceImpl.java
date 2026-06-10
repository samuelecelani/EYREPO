package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.StatoEnum;
import it.ey.enums.TypeErrorEnum;
import it.ey.piao.api.exception.BusinessException;
import it.ey.piao.api.mapper.OVPStrategiaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IIndicatoreService;
import it.ey.piao.api.service.IOVPStrategiaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.enums.Sezione;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.ey.piao.api.service.impl.DeleteDependencyService.STATI_BLOCCANTI;

@Service
public class OVPStrategiaServiceImpl implements IOVPStrategiaService {

    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaServiceImpl.class);

    private final IOVPStrategiaRepository iovpStrategiaRepository;
    private final OVPRepository ovpRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IIndicatoreRepository indicatoreRepository;
    private final OVPStrategiaMapper ovpStrategiaMapper;
    private final IIndicatoreService indicatoreService;
    private final DeleteDependencyService deleteDependencyService;
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
    private  final  IObbiettivoPerformanceRepository obbiettivoPerformanceRepository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final IOVPStrategiaIndicatoreRepository ovpStrategiaIndicatoreRepository;

    public OVPStrategiaServiceImpl(IOVPStrategiaRepository iovpStrategiaRepository,
                                   OVPRepository ovpRepository,
                                   ApplicationEventPublisher eventPublisher,
                                   IIndicatoreRepository indicatoreRepository,
                                   OVPStrategiaMapper ovpStrategiaMapper,
                                   IIndicatoreService indicatoreService,
                                   DeleteDependencyService deleteDependencyService, IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository, IObbiettivoPerformanceRepository obbiettivoPerformanceRepository, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository, IOVPStrategiaIndicatoreRepository ovpStrategiaIndicatoreRepository) {

        this.iovpStrategiaRepository = iovpStrategiaRepository;
        this.ovpRepository = ovpRepository;
        this.eventPublisher = eventPublisher;
        this.indicatoreRepository = indicatoreRepository;
        this.ovpStrategiaMapper = ovpStrategiaMapper;
        this.indicatoreService = indicatoreService;
        this.deleteDependencyService = deleteDependencyService;
        this.obiettivoPrevenzioneCorruzioneTrasparenzaRepository = obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
        this.obbiettivoPerformanceRepository = obbiettivoPerformanceRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.ovpStrategiaIndicatoreRepository = ovpStrategiaIndicatoreRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 20)
    public OVPStrategiaDTO save(OVPStrategiaDTO request, Long idOVP) {
        if(request == null){
            throw new IllegalArgumentException("OVPStrategiaDTO è obbligatorio");
        }
        if(idOVP == null){
            throw new IllegalArgumentException("idOVP è obbligatorio");
        }

        OVPStrategiaDTO response;

        try {
            log.debug("Salvataggio OVPStrategiaDTO per OVP con id: {}", idOVP);

            // Mappo il DTO in entity
            OVPStrategia entity = ovpStrategiaMapper.toEntity(request, new CycleAvoidingMappingContext());

            // Salvo lo stato dell'oggetto per un eventuale rollback
            if (entity.getId() != null) {
                iovpStrategiaRepository.findById(entity.getId()).ifPresent(existing ->
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(OVPStrategia.class, existing)));
            }
            // Setto l'OVP recuperata nell'entity
            entity.setOvp(ovpRepository.getReferenceById(idOVP));

            // Sincronizzazione Indicatori (OneToMany)
            syncIndicatori(entity, request.getIndicatori());

            // Salvo l'entity
            OVPStrategia saved = iovpStrategiaRepository.save(entity);

            log.info("OVPStrategia salvata con id: {} per OVP id: {}", saved.getId(), idOVP);

            // Mapping del risultato
            response = ovpStrategiaMapper.toDto(saved, new CycleAvoidingMappingContext());

            // Popolo manualmente la lista indicatori dalla savedEntity
            populateIndicatoriDTO(response, saved);

            // Salvataggio storico modifiche
            if (request.getCampiModificati() != null && !request.getCampiModificati().isBlank() && request.getIdPiao() != null) {
                OVP ovp = saved.getOvp();
                Long idSezione = (ovp != null && ovp.getSezione21() != null) ? ovp.getSezione21().getId() : null;
                if (idSezione != null) {
                    storicoModificaHelper.salvaStoricoSePresente(request, idSezione, request.getIdPiao(), Sezione.SEZIONE_21);
                }
            }
            if (request.getStatoSezione() != null && !request.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(request.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(saved.getOvp().getSezione21().getId(),Sezione.SEZIONE_21.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(request.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(saved.getOvp().getSezione21().getId())
                        .codTipologiaFK(Sezione.SEZIONE_21.name())
                        .testo(StatoEnum.fromDescrizione(request.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(request.getUpdatedByNameSurname())
                        .createdByRole(request.getUpdatedByRole())
                        .build());
            }
            // Pubblicazione evento di successo dopo il commit
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (DataAccessException dae) {
            log.error("Errore DB in save (OVPStrategia): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello OVPStrategia", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (OVPStrategia): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello OVPStrategia", e);
        }
        return response;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public void saveAll(List<OVPStrategiaDTO> requests, Long idOVP) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("La lista di richieste non può essere nulla o vuota");
        }
        if (idOVP == null) {
            throw new IllegalArgumentException("idOVP è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            log.info("Salvataggio batch di {} OVPStrategia per OVP id={}", requests.size(), idOVP);

            // Recupera il riferimento all'OVP padre
            OVP ovpParent = ovpRepository.getReferenceById(idOVP);

            // Batch loading degli ID esistenti per BeforeUpdate events
            List<Long> existingIds = requests.stream()
                .filter(r -> r != null && r.getId() != null)
                .map(OVPStrategiaDTO::getId)
                .distinct()
                .toList();

            List<OVPStrategia> existingEntities = existingIds.isEmpty()
                ? new ArrayList<>()
                : iovpStrategiaRepository.findAllById(existingIds);

            existingEntities.forEach(existing ->
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(OVPStrategia.class, existing))
            );

            List<OVPStrategia> entitiesToSave = new ArrayList<>();

            // Fase 1: Preparazione entità con mapping e sincronizzazione
            for (OVPStrategiaDTO request : requests) {
                if (request == null) {
                    log.warn("Elemento null trovato nella lista, verrà saltato");
                    continue;
                }

                // Mapping del DTO all'entità
                OVPStrategia entity = ovpStrategiaMapper.toEntity(request, context);

                // Impostazione della relazione con OVP padre
                entity.setOvp(ovpParent);

                // Sincronizzazione Indicatori (OneToMany)
                syncIndicatori(entity, request.getIndicatori());

                entitiesToSave.add(entity);
            }

            // Fase 2: Salvataggio batch di tutte le entità
            List<OVPStrategia> savedEntities = iovpStrategiaRepository.saveAll(entitiesToSave);

            log.info("Salvate con successo {} OVPStrategia per OVP id={}", savedEntities.size(), idOVP);

            // Salvataggio storico modifiche per ogni request che ha campiModificati
            for (OVPStrategiaDTO request : requests) {
                if (request != null && request.getCampiModificati() != null && !request.getCampiModificati().isBlank() && request.getIdPiao() != null) {
                    OVP ovp = ovpParent;
                    Long idSezione = (ovp.getSezione21() != null) ? ovp.getSezione21().getId() : null;
                    if (idSezione != null) {
                        storicoModificaHelper.salvaStoricoSePresente(request, idSezione, request.getIdPiao(), Sezione.SEZIONE_21);
                    }
                }
            }

            // Pubblicazione evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedEntities));

        }
        catch (Exception e) {
            log.error("Errore inatteso in saveAll (OVPStrategia) per OVP id={}: {}", idOVP, e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch delle OVPStrategia", e);
        }
    }

    /**
     * Estrae in sicurezza l'idPiao da OVPStrategia senza generare LazyInitializationException.
     */
    private Long extractIdPiao(OVPStrategia strategia) {
        if (strategia == null) return null;
        var ovp = strategia.getOvp();
        if (ovp == null) return null;
        var s21 = ovp.getSezione21();
        if (s21 == null) return null;
        var piao = s21.getPiao();
        return (piao != null ? piao.getId() : null);
    }

    /**
     * Estrae id della sezione 21 evitando null pointer e lazy load dopo delete.
     */
    private Long extractIdSezione21(OVPStrategia strategia) {
        if (strategia == null) return null;
        var ovp = strategia.getOvp();
        if (ovp == null) return null;
        var s21 = ovp.getSezione21();
        return (s21 != null ? s21.getId() : null);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 10)
    public void delete(Long id,
                       String campiModificati,
                       Long idPiao,
                       String testoSezione,
                       String updatedByNameSurname,
                       String updatedByRole,
                       boolean forceDelete,
                       String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("ID è obbligatorio");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            log.debug("Eliminazione OVPStrategia con id: {} (forceDelete={})", id, forceDelete);

            OVPStrategia strategia = iovpStrategiaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                    BusinessException.INTERNAL_ERROR,
                    "OVPStrategia con id " + id + " non trovata",
                    TypeErrorEnum.ERROR
                ));

            Long idPiaos = extractIdPiao(strategia);
            Long idSezione21 = extractIdSezione21(strategia);

            OVPStrategiaDTO dto = ovpStrategiaMapper.toDto(strategia, context);

            dto.setIdPiao(idPiaos);



                deleteDependencyService.validateBeforeDeleteOrUpdate(id, dto, forceDelete);

                if (dto.getTypeEnum() == TypeErrorEnum.ERROR) {
                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.ERROR);
                }

                if (dto.getTypeEnum() == TypeErrorEnum.WARNING) {

                    throw new BusinessException(dto.getErrorCode(), dto.getMessageError(), TypeErrorEnum.WARNING);
                }



            LocalDateTime deactivationTime = LocalDateTime.now();
                // SOFT DELETE tabella associativa
            ovpStrategiaIndicatoreRepository.softDeleteByOvpStrategiaId(id,deactivationTime);


            iovpStrategiaRepository.softDeleteById(id, deactivationTime);

            /*
            strategia.setActive(false);
            strategia.setDeactivationTime(deactivationTime);

             */

            log.info("OVPStrategia con id {} eliminata con successo", id);

            if (idSezione21 != null &&
                campiModificati != null && !campiModificati.isBlank() &&
                idPiaos != null) {

                BaseDTO dtoStorico = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiaos)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(
                    dtoStorico,
                    idSezione21,
                    idPiaos,
                    Sezione.SEZIONE_21
                );
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank() && idSezione21 != null) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione21, Sezione.SEZIONE_21.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(idSezione21)
                            .codTipologiaFK(Sezione.SEZIONE_21.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
                forceDelete = true;
            }

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(strategia));

        } catch (BusinessException ex) {
            throw ex;

        } catch (DataAccessException dae) {
            log.error("Errore DB in delete (OVPStrategia) id={}: {}", id, dae.getMessage(), dae);
            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore di persistenza durante l'eliminazione della Strategia",
                TypeErrorEnum.ERROR
            );

        } catch (Exception e) {
            log.error("Errore inatteso in delete (OVPStrategia) id={}: {}", id, e.getMessage(), e);
            throw new BusinessException(
                BusinessException.INTERNAL_ERROR,
                "Errore interno durante l'eliminazione della Strategia",
                TypeErrorEnum.ERROR
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OVPStrategiaDTO> findByOvpId(Long idOvp) {
        if (idOvp == null) {
            throw new IllegalArgumentException("idOvp è obbligatorio");
        }

        try {
            log.debug("Recupero strategie per OVP con id: {}", idOvp);
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            List<OVPStrategia> strategie = iovpStrategiaRepository.findByOvpId(idOvp);

            if (strategie.isEmpty()) {
                log.info("Nessuna strategia trovata per OVP id={}", idOvp);
                return new ArrayList<>();
            }

            // Mapping e caricamento dati MongoDB per ogni strategia
            return strategie.stream()
                .map(entity -> {
                    OVPStrategiaDTO dto = ovpStrategiaMapper.toDto(entity, context);
                    // Popolo la lista indicatori
                    populateIndicatoriDTO(dto, entity);
                    // Carica i dati MongoDB per ogni strategia
                    loadMongoDataForStrategia(dto);
                    return dto;
                })
                .toList();

        } catch (Exception e) {
            log.error("Errore durante il recupero delle strategie per OVP id={}: {}", idOvp, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle strategie", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public OVPStrategiaDTO enrichWithRelations(OVPStrategia entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("L'entità OVPStrategia e l'ID non possono essere nulli");
        }

        try {
            // Mappa entity -> DTO
            OVPStrategiaDTO dto = ovpStrategiaMapper.toDto(entity, new CycleAvoidingMappingContext());

            // Popolo la lista di indicatori con i dati completi
            populateIndicatoriDTO(dto, entity);

            log.debug("OVPStrategiaDTO con id={} arricchito con relazioni", entity.getId());
            return dto;
        } catch (Exception e) {
            log.error("Errore durante l'arricchimento dell'OVPStrategiaDTO con id={}: {}",
                entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'arricchimento dell'OVPStrategiaDTO con le relazioni", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void loadMongoDataForStrategia(OVPStrategiaDTO strategiaDTO) {
        if (strategiaDTO == null || strategiaDTO.getId() == null) {
            log.warn("OVPStrategiaDTO o ID è null, skip caricamento MongoDB");
            return;
        }

        try {
            // Carica i dati MongoDB per ogni Indicatore della strategia
            if (strategiaDTO.getIndicatori() != null && !strategiaDTO.getIndicatori().isEmpty()) {
                // OTTIMIZZAZIONE: Raccogli tutti gli ID degli indicatori
                List<Long> indicatoriIds = strategiaDTO.getIndicatori().stream()
                    .filter(indDTO -> indDTO.getIndicatore() != null && indDTO.getIndicatore().getId() != null)
                    .map(indDTO -> indDTO.getIndicatore().getId())
                    .distinct()
                    .toList();

                if (!indicatoriIds.isEmpty()) {
                    // Carica TUTTI gli indicatori in UNA SOLA query batch
                    Map<Long, IndicatoreDTO> indicatoriMap = indicatoreService.findAllByIdsWithRelations(indicatoriIds);

                    // Assegna gli indicatori caricati ai DTO
                    strategiaDTO.getIndicatori().forEach(indDTO -> {
                        if (indDTO.getIndicatore() != null && indDTO.getIndicatore().getId() != null) {
                            IndicatoreDTO indicatoreCompleto = indicatoriMap.get(indDTO.getIndicatore().getId());
                            if (indicatoreCompleto != null) {
                                indDTO.setIndicatore(indicatoreCompleto);
                            }
                        }
                    });
                }
            }

            log.debug("Dati MongoDB caricati per OVPStrategia id={}", strategiaDTO.getId());
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per OVPStrategia id={}: {}",
                strategiaDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB OVPStrategia", e);
        }
    }

    /**
     * Sincronizza la lista di Indicatori della strategia.
     * Gli indicatori esistono già nel DB, va solo settato il riferimento all'obiettivo.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni.
     */
    @Override
    public void syncIndicatori(OVPStrategia parent, List<OVPStrategiaIndicatoreDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setIndicatori(new ArrayList<>());
            return;
        }

        List<OVPStrategiaIndicatore> entities = new ArrayList<>();

        for (OVPStrategiaIndicatoreDTO dto : dtoList) {
            OVPStrategiaIndicatore entity = OVPStrategiaIndicatore.builder()
                .id(dto.getId())
                .ovpStrategia(parent)
                .indicatore(indicatoreRepository.getReferenceById(dto.getIndicatore().getId()))
                .build();
            entities.add(entity);
        }

        parent.setIndicatori(entities);
    }


    /**
     * Popola manualmente la lista di Indicatori nel DTO dalla entity salvata.
     * Converte OVPStrategia -> OVPStrategiaDTO.
     * Recupera anche addInfo (UlterioriInfo) da MongoDB per ogni indicatore.
     */
    private void populateIndicatoriDTO(OVPStrategiaDTO dto, OVPStrategia entity) {
        if (entity.getIndicatori() != null && !entity.getIndicatori().isEmpty()) {
            List<OVPStrategiaIndicatoreDTO> indicatoriDTO = entity.getIndicatori().stream()
                .map(ind -> {
                    OVPStrategiaIndicatoreDTO indDTO = new OVPStrategiaIndicatoreDTO();
                    indDTO.setId(ind.getId());

                    // Usa enrichWithRelations del servizio Indicatore per ottenere tutti i dati
                    IndicatoreDTO indicatoreDTO = indicatoreService.enrichWithRelations(ind.getIndicatore());

                    indDTO.setIndicatore(indicatoreDTO);
                    return indDTO;
                })
                .toList();
            dto.setIndicatori(indicatoriDTO);
        }
    }
}
