package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.AreaOrganizzativaMapper;
import it.ey.piao.api.mapper.OVPMapper;
import it.ey.piao.api.mapper.PrioritaPoliticaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IIndicatoreService;
import it.ey.piao.api.service.IOVPService;
import it.ey.piao.api.service.IOVPStrategiaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OVPServiceImpl implements IOVPService {

    private static final Logger log = LoggerFactory.getLogger(OVPServiceImpl.class);

    private final OVPRepository ovpRepository;
    private final OVPMapper ovpMapper;
    private final PrioritaPoliticaMapper prioritaPoliticaMapper;
    private final AreaOrganizzativaMapper areaOrganizzativaMapper;
    private final ISezione21Repository sezione21Repository;
    private final IAreaOrganizzativaRepository areaOrganizzativaRepository;
    private final IPrioritaPoliticaRepository prioritaPoliticaRepository;
    private final IStakeHolderRepository stakeHolderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IOVPStrategiaService ovpStrategiaService;
    private final IIndicatoreService indicatoreService;
    private final DeleteDependencyService deleteDependencyService;

    public OVPServiceImpl(OVPRepository ovpRepository, OVPMapper ovpMapper,
                          PrioritaPoliticaMapper prioritaPoliticaMapper,
                          AreaOrganizzativaMapper areaOrganizzativaMapper,
                          ISezione21Repository sezione21Repository,
                          IAreaOrganizzativaRepository areaOrganizzativaRepository,
                          IPrioritaPoliticaRepository prioritaPoliticaRepository,
                          IStakeHolderRepository stakeHolderRepository,
                          ApplicationEventPublisher eventPublisher,
                          IOVPStrategiaService ovpStrategiaService,
                          IIndicatoreService indicatoreService,
                          DeleteDependencyService deleteDependencyService) {
        this.ovpRepository = ovpRepository;
        this.ovpMapper = ovpMapper;
        this.prioritaPoliticaMapper = prioritaPoliticaMapper;
        this.areaOrganizzativaMapper = areaOrganizzativaMapper;
        this.sezione21Repository = sezione21Repository;
        this.areaOrganizzativaRepository = areaOrganizzativaRepository;
        this.prioritaPoliticaRepository = prioritaPoliticaRepository;
        this.stakeHolderRepository = stakeHolderRepository;
        this.eventPublisher = eventPublisher;
        this.ovpStrategiaService = ovpStrategiaService;
        this.indicatoreService = indicatoreService;
        this.deleteDependencyService = deleteDependencyService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public void saveOrUpdate(OVPDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Mapping del DTO all'entità
            OVP entity = ovpMapper.toEntity(request, context);

            // Salvo lo stato dell'oggetto per un eventuale rollback
            if (entity.getId() != null) {
                ovpRepository.findById(entity.getId()).ifPresent(existing -> {
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(OVP.class, existing));
                });
            }

            // Impostazione delle relazioni usando getReferenceById
            entity.setSezione21(sezione21Repository.getReferenceById(request.getSezione21Id()));

            // Sincronizzazione Aree Organizzative (OneToMany)
            syncAreeOrganizzative(entity, request.getAreeOrganizzative());

            // Sincronizzazione Priorità Politiche (OneToMany)
            syncPrioritaPolitiche(entity, request.getPrioritaPolitiche());

            // Sincronizzazione StakeHolders (OneToMany)
            syncStakeHolders(entity, request.getStakeholders());

            // Sincronizzazione Risorse Finanziarie (OneToMany)
            syncRisorseFinanziarie(entity, request.getRisorseFinanziarie());

            // Sincronizzazione Strategie (OneToMany) - possono essere nuove o esistenti
            syncStrategie(entity, request.getOvpStrategias());

            // Salvataggio dell'entità principale (Hibernate gestisce insert/update automaticamente)
            OVP savedEntity = ovpRepository.save(entity);

            // Pubblicazione evento di successo dopo il commit
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(savedEntity));

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate OVP per id={}: {}",
                request.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(ovpMapper.toEntity(request, context), e)
            );
            throw new RuntimeException("Errore durante il salvataggio o aggiornamento dell'OVP", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public List<OVPDTO> saveOrUpdateAll(List<OVPDTO> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("La lista di richieste non può essere nulla o vuota");
        }

         List<OVPDTO> responses = new ArrayList<>();
         CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {
            log.info("Salvataggio batch di {} OVP", requests.size());

            // Batch loading degli ID esistenti per BeforeUpdate events
            List<Long> existingIds = requests.stream()
                .filter(r -> r != null && r.getId() != null)
                .map(OVPDTO::getId)
                .distinct()
                .toList();

            List<OVP> existingEntities = existingIds.isEmpty()
                ? new ArrayList<>()
                : ovpRepository.findAllById(existingIds);

            existingEntities.forEach(existing ->
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(OVP.class, existing))
            );

            List<OVP> entitiesToSave = new ArrayList<>();

            // Fase 1: Preparazione entità con mapping e sincronizzazione back-reference
            for (OVPDTO request : requests) {
                if (request == null) {
                    log.warn("Elemento null trovato nella lista, verrà saltato");
                    continue;
                }

                // Mapping del DTO all'entità
                OVP entity = ovpMapper.toEntity(request, context);

                // Impostazione delle relazioni usando getReferenceById (lazy proxy)
                entity.setSezione21(sezione21Repository.getReferenceById(request.getSezione21Id()));

                // Sincronizzazione relazioni con i metodi sync (impostano i back-reference)
                syncAreeOrganizzative(entity, request.getAreeOrganizzative());
                syncPrioritaPolitiche(entity, request.getPrioritaPolitiche());
                syncStakeHolders(entity, request.getStakeholders());
                syncRisorseFinanziarie(entity, request.getRisorseFinanziarie());
                syncStrategie(entity, request.getOvpStrategias());

                entitiesToSave.add(entity);
            }

            // Fase 2: Salvataggio batch di tutte le entità
            List<OVP> savedEntities = ovpRepository.saveAll(entitiesToSave);

            // Fase 3: Mapping delle entità salvate a DTO - MapStruct gestisce automaticamente tutte le relazioni
            for (OVP savedEntity : savedEntities) {
                responses.add(ovpMapper.toDto(savedEntity, context));
            }

            log.info("Salvati con successo {} OVP su {}", responses.size(), requests.size());

            // Pubblicazione evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(responses));

            return responses;

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdateAll di {} OVP: {}",
                requests.size(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch degli OVP", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OVPDTO> findAllOVPBySezione(Long idSezione) {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        // Query semplice e veloce senza JOIN FETCH
        List<OVP> ovps = ovpRepository.findBySezione21Id(idSezione);

        if (ovps.isEmpty()) {
            return new ArrayList<>();
        }

        // Mapping diretto e caricamento dati MongoDB per ogni OVP
        return ovps.stream()
            .map(o -> {
                OVPDTO dto = ovpMapper.toDto(o, context);
                // Carica i dati MongoDB per ogni OVP (indicatori con UlterioriInfo MongoDB)
                return loadMongoDataForOVP(dto);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OVPDTO> findAllOVPByPiao(Long piaoId) {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        // Query semplice e veloce senza JOIN FETCH
        List<OVP> ovps = ovpRepository.findByPiaoId(piaoId);

        if (ovps.isEmpty()) {
            return new ArrayList<>();
        }

        // Mapping diretto e caricamento dati MongoDB per ogni OVP
        return ovps.stream()
            .map(o -> {
                OVPDTO dto = ovpMapper.toDto(o, context);
                // Carica i dati MongoDB per ogni OVP (indicatori con UlterioriInfo MongoDB)
                return loadMongoDataForOVP(dto);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 10)
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id OVP non può essere null");
        }
        if (!ovpRepository.existsById(id)) {
            throw new IllegalArgumentException("OVP non trovata: " + id);
        }
        try {
            log.info("Eliminazione OVP con ID={}", id);

            // *** STEP 1: Recupera le strategie collegate e il Piao ***
            List<OVPStrategiaDTO> strategieCollegate = ovpStrategiaService.findByOvpId(id);
            List<Long> strategieIds = strategieCollegate.stream()
                    .map(OVPStrategiaDTO::getId)
                    .filter(Objects::nonNull)
                    .toList();

            // Recupera il Piao dall'OVP per il cambio stato sezioni
            OVP ovp = ovpRepository.findById(id).orElse(null);
            Piao piao = (ovp != null && ovp.getSezione21() != null) ? ovp.getSezione21().getPiao() : null;

            // *** STEP 2: Nullifica dipendenze e cambia stato sezioni (centralizzato) ***
            deleteDependencyService.handleOvpDeleteDependencies(id, piao, strategieIds);

            // *** STEP 3: Procede con la cancellazione dell'OVP (cancellerà anche le strategie) ***
            ovpRepository.deleteById(id);
            log.info("OVP con ID={} eliminato con successo (insieme a {} strategie)", id, strategieCollegate.size());
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(id));
        } catch (DataIntegrityViolationException ex) {
            log.error("Errore di integrità durante l'eliminazione dell'OVP ID={}: {}", id, ex.getMessage());
            eventPublisher.publishEvent(new TransactionFailureEvent<>(id, ex));
            throw new RuntimeException("Impossibile eliminare l'OVP per vincoli di integrità (FK). Id: " + id, ex);
        } catch (Exception ex) {
            log.error("Errore durante l'eliminazione dell'OVP ID={}: {}", id, ex.getMessage(), ex);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(id, ex));
            throw new RuntimeException("Errore durante l'eliminazione dell'OVP. Id: " + id, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OVPMatriceDataDTO findOVPMatriceData(Long idSezione, Long idSezione1, Long idPiao) {
        if (idSezione == null && idPiao == null) {
            throw new IllegalArgumentException("Almeno uno tra idSezione e idPiao deve essere valorizzato");
        }

        log.info("Recupero dati matrice OVP per Sezione21 ID={} o PIAO ID={}", idSezione, idPiao);
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        // 1. Recupera tutti gli OVP: usa idSezione se disponibile, altrimenti usa idPiao
        List<OVP> ovpEntities = idSezione != null
            ? ovpRepository.findBySezione21Id(idSezione)
            : ovpRepository.findByPiaoId(idPiao);

        List<OVPDTO> ovpList = ovpEntities.stream()
            .map(o -> ovpMapper.toDto(o, context))
            .collect(Collectors.toList());

        // 2. Recupera TUTTE le priorità politiche (non solo quelle collegate agli OVP)
        List<PrioritaPoliticaDTO> allPrioritaPolitiche = prioritaPoliticaRepository.findBySezione1Id(idSezione1)
            .stream()
            .map(pp -> prioritaPoliticaMapper.toDto(pp, context))
            .collect(Collectors.toList());

        // 3. Recupera TUTTE le aree organizzative (non solo quelle collegate agli OVP)
        List<AreaOrganizzativaDTO> allAreeOrganizzative = areaOrganizzativaRepository.findBySezione1Id(idSezione1)
            .stream()
            .map(ao -> areaOrganizzativaMapper.toDto(ao, context))
            .collect(Collectors.toList());

        log.info("Trovati {} OVP, {} Priorità Politiche, {} Aree Organizzative",
                 ovpList.size(), allPrioritaPolitiche.size(), allAreeOrganizzative.size());

        return OVPMatriceDataDTO.builder()
            .ovpList(ovpList)
            .allPrioritaPolitiche(allPrioritaPolitiche)
            .allAreeOrganizzative(allAreeOrganizzative)
            .build();
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public OVPDTO enrichWithRelations(OVP entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("L'entità OVP e l'ID non possono essere nulli");
        }

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
            // MapStruct gestisce automaticamente tutte le relazioni nel toDto
            OVPDTO dto = ovpMapper.toDto(entity, context);

            log.debug("OVPDTO con id={} arricchito con relazioni", entity.getId());
            return dto;
        } catch (Exception e) {
            log.error("Errore durante l'arricchimento dell'OVPDTO con id={}: {}",
                entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'arricchimento dell'OVPDTO con le relazioni", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OVPDTO loadMongoDataForOVP(OVPDTO ovpDTO) {
        if (ovpDTO == null || ovpDTO.getId() == null) {
            log.warn("OVPDTO o ID è null, skip caricamento MongoDB");
            return ovpDTO;
        }

        try {
            // SUPER OTTIMIZZAZIONE: Raccogli TUTTI gli ID degli indicatori da TUTTE le strategie
            if (ovpDTO.getOvpStrategias() != null && !ovpDTO.getOvpStrategias().isEmpty()) {
                List<Long> allIndicatoriIds = ovpDTO.getOvpStrategias().stream()
                    .filter(strategia -> strategia.getIndicatori() != null && !strategia.getIndicatori().isEmpty())
                    .flatMap(strategia -> strategia.getIndicatori().stream())
                    .filter(indDTO -> indDTO.getIndicatore() != null && indDTO.getIndicatore().getId() != null)
                    .map(indDTO -> indDTO.getIndicatore().getId())
                    .distinct()
                    .toList();

                // Carica TUTTI gli indicatori in UNA SOLA query batch
                if (!allIndicatoriIds.isEmpty()) {
                    Map<Long, IndicatoreDTO> indicatoriMap = indicatoreService.findAllByIdsWithRelations(allIndicatoriIds);

                    // Assegna gli indicatori caricati a tutte le strategie
                    ovpDTO.getOvpStrategias().forEach(strategia -> {
                        if (strategia.getIndicatori() != null) {
                            strategia.getIndicatori().forEach(indDTO -> {
                                if (indDTO.getIndicatore() != null && indDTO.getIndicatore().getId() != null) {
                                    IndicatoreDTO indicatoreCompleto = indicatoriMap.get(indDTO.getIndicatore().getId());
                                    if (indicatoreCompleto != null) {
                                        indDTO.setIndicatore(indicatoreCompleto);
                                    }
                                }
                            });
                        }
                    });

                    log.debug("Caricati {} indicatori in batch per OVP id={}", allIndicatoriIds.size(), ovpDTO.getId());
                }
            }

            log.debug("Dati MongoDB caricati per OVP id={}", ovpDTO.getId());
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per OVP id={}: {}", ovpDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB OVP", e);
        }
        return ovpDTO;
    }

    /**
     * Sincronizza la lista di Aree Organizzative dell'OVP.
     * Le aree esistono già nel DB, va solo settato il riferimento all'OVP.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni.
     */
    private void syncAreeOrganizzative(OVP parent, List<OVPAreaOrganizzativaDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setAreeOrganizzative(new ArrayList<>());
            return;
        }

        List<OVPAreaOrganizzativa> entities = new ArrayList<>();
        for (OVPAreaOrganizzativaDTO dto : dtoList) {
            OVPAreaOrganizzativa entity = OVPAreaOrganizzativa.builder()
                .id(dto.getId()) // Preserva l'ID per evitare duplicate key
                .ovp(parent)
                .areaOrganizzativa(areaOrganizzativaRepository.getReferenceById(dto.getAreaOrganizzativa().getId()))
                .build();
            entities.add(entity);
        }
        parent.setAreeOrganizzative(entities);
    }

    /**
     * Sincronizza la lista di Priorità Politiche dell'OVP.
     */
    private void syncPrioritaPolitiche(OVP parent, List<OVPPrioritaPoliticaDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setPrioritaPolitiche(new ArrayList<>());
            return;
        }

        List<OVPPrioritaPolitica> entities = new ArrayList<>();
        for (OVPPrioritaPoliticaDTO dto : dtoList) {
            OVPPrioritaPolitica entity = OVPPrioritaPolitica.builder()
                .id(dto.getId()) // Preserva l'ID per evitare duplicate key
                .ovp(parent)
                .prioritaPolitica(prioritaPoliticaRepository.getReferenceById(dto.getPrioritaPolitica().getId()))
                .build();
            entities.add(entity);
        }
        parent.setPrioritaPolitiche(entities);
    }

    /**
     * Sincronizza la lista di StakeHolders dell'OVP.
     */
    private void syncStakeHolders(OVP parent, List<OVPStakeHolderDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setStakeholders(new ArrayList<>());
            return;
        }

        List<OVPStakeHolder> entities = new ArrayList<>();
        for (OVPStakeHolderDTO dto : dtoList) {
            OVPStakeHolder entity = OVPStakeHolder.builder()
                .id(dto.getId()) // Preserva l'ID per evitare duplicate key
                .ovp(parent)
                .stakeholder(stakeHolderRepository.getReferenceById(dto.getStakeholder().getId()))
                .build();
            entities.add(entity);
        }
        parent.setStakeholders(entities);
    }

    /**
     * Sincronizza la lista di Risorse Finanziarie dell'OVP.
     */
    private void syncRisorseFinanziarie(OVP parent, List<OVPRisorsaFinanziariaDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setRisorseFinanziarie(new ArrayList<>());
            return;
        }

        List<OVPRisorsaFinanziaria> entities = new ArrayList<>();
        for (OVPRisorsaFinanziariaDTO dto : dtoList) {
            OVPRisorsaFinanziaria entity = OVPRisorsaFinanziaria.builder()
                .id(dto.getId()) // Preserva l'ID per evitare duplicate key
                .ovp(parent)
                .iniziativa(dto.getIniziativa())
                .descrizione(dto.getDescrizione())
                .dotazioneFinanziaria(dto.getDotazioneFinanziaria())
                .fonteFinanziamento(dto.getFonteFinanziamento())
                .build();

            entities.add(entity);
        }
        parent.setRisorseFinanziarie(entities);
    }

    /**
     * Sincronizza la lista di Strategie dell'OVP.
     * Le strategie possono essere nuove (senza ID) o esistenti (con ID).
     * Se hanno ID, viene preservato per evitare duplicate key.
     * Hibernate gestisce automaticamente insert/update grazie ai cascade.
     * Gli indicatori vengono sincronizzati tramite OVPStrategiaService.
     */
    private void syncStrategie(OVP parent, List<OVPStrategiaDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setOvpStrategias(new ArrayList<>());
            return;
        }

        List<OVPStrategia> entities = new ArrayList<>();
        for (OVPStrategiaDTO dto : dtoList) {
            OVPStrategia entity = OVPStrategia.builder()
                .id(dto.getId()) // Preserva l'ID per evitare duplicate key
                .ovp(parent)
                .codStrategia(dto.getCodStrategia())
                .denominazioneStrategia(dto.getDenominazioneStrategia())
                .descrizioneStrategia(dto.getDescrizioneStrategia())
                .soggettoResponsabile(dto.getSoggettoResponsabile())
                .build();

            // Sincronizzazione indicatori tramite OVPStrategiaService

            ovpStrategiaService.syncIndicatori(entity, dto.getIndicatori());

            entities.add(entity);
        }
        parent.setOvpStrategias(entities);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int updateValoreIndiceAndDescrizione(Long idOvp, Long valoreIndice, String descrizioneIndice) {
        if (idOvp == null) {
            throw new IllegalArgumentException("ID OVP non può essere null");
        }

        try {
            log.info("Aggiornamento valoreIndice={} e descrizioneIndice per OVP id={}", valoreIndice, idOvp);
            int rowsAffected = ovpRepository.updateValoreIndiceAndDescrizione(idOvp, valoreIndice, descrizioneIndice);

            if (rowsAffected == 0) {
                log.warn("Nessun OVP trovato con id={}, nessun aggiornamento effettuato", idOvp);
            } else {
                log.info("OVP id={} aggiornato con successo: {} riga/e modificata/e", idOvp, rowsAffected);
            }

            return rowsAffected;
        } catch (Exception e) {
            log.error("Errore durante l'aggiornamento di valoreIndice e descrizioneIndice per OVP id={}: {}",
                idOvp, e.getMessage(), e);
            throw new RuntimeException("Errore durante l'aggiornamento dell'OVP", e);
        }
    }
}
