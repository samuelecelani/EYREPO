package it.ey.piao.api.service.impl;

import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;
import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO;
import it.ey.entity.*;
import it.ey.piao.api.mapper.IObiettivoPrevenzioneCorruzioneTrasparenzaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IObiettivoPrevenzioneCorruzioneTrasparenzaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl implements IObiettivoPrevenzioneCorruzioneTrasparenzaService {

    private static final Logger log = LoggerFactory.getLogger(ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl.class);

    private final IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
    private final OVPRepository ovpRepository;
    private final IOVPStrategiaRepository ovpStrategiaRepository;
    private final IObbiettivoPerformanceRepository obbiettivoPerformanceRepository;
    private final ISezione23Repository sezione23Repository;
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaMapper mapper;
    private final IIndicatoreRepository indicatoreRepository;
    private final IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl(
            IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository,
            OVPRepository ovpRepository,
            IOVPStrategiaRepository ovpStrategiaRepository,
            IObbiettivoPerformanceRepository obbiettivoPerformanceRepository,
            ISezione23Repository sezione23Repository,
            IObiettivoPrevenzioneCorruzioneTrasparenzaMapper mapper,
            IIndicatoreRepository indicatoreRepository,
            IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository,
            IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.obiettivoPrevenzioneCorruzioneTrasparenzaRepository = obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
        this.ovpRepository = ovpRepository;
        this.ovpStrategiaRepository = ovpStrategiaRepository;
        this.obbiettivoPerformanceRepository = obbiettivoPerformanceRepository;
        this.sezione23Repository = sezione23Repository;
        this.mapper = mapper;
        this.indicatoreRepository = indicatoreRepository;
        this.misuraPrevenzioneEventoRischioRepository = misuraPrevenzioneEventoRischioRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public ObiettivoPrevenzioneCorruzioneTrasparenzaDTO  saveOrUpdate(ObiettivoPrevenzioneCorruzioneTrasparenzaDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Mapping del DTO all'entità
            ObiettivoPrevenzioneCorruzioneTrasparenza entity = mapper.toEntity(request, context);



            // Impostazione delle relazioni usando getReferenceById
            entity.setSezione23(sezione23Repository.getReferenceById(request.getIdSezione23()));

            // Relazioni opzionali: possono essere null
            if (request.getIdOVP() != null) {
                entity.setOvp(ovpRepository.getReferenceById(request.getIdOVP()));
            }
            if (request.getIdStrategiaOVP() != null) {
                entity.setOvpStrategia(ovpStrategiaRepository.getReferenceById(request.getIdStrategiaOVP()));
            }
            if (request.getIdObbiettivoPerformance() != null) {
                entity.setObbiettivoPerformance(obbiettivoPerformanceRepository.getReferenceById(request.getIdObbiettivoPerformance()));
            }

            // Sincronizzazione Indicatori (OneToMany)
            syncIndicatori(entity, request.getIndicatori());

            // Salvataggio dell'entità principale (Hibernate gestisce insert/update automaticamente)
            ObiettivoPrevenzioneCorruzioneTrasparenza savedEntity = obiettivoPrevenzioneCorruzioneTrasparenzaRepository.save(entity);
return mapper.toDto(savedEntity, context);

        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate ObiettivoPrevenzioneCorruzioneTrasparenza per id={}: {}",
                    request.getId(), e.getMessage(), e);

            throw new RuntimeException("Errore durante il salvataggio o aggiornamento dell'ObiettivoPrevenzioneCorruzioneTrasparenza", e);
        }


    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 60)
    public void  saveAll(List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Errore durante il salvataggio  degli ObiettivoPrevenzioneCorruzioneTrasparenza la richiesta è null");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            List<ObiettivoPrevenzioneCorruzioneTrasparenza> entitiesToSave = new ArrayList<>();

            for (ObiettivoPrevenzioneCorruzioneTrasparenzaDTO request : requests) {
                if (request == null) {
                    continue;
                }

                // Mapping del DTO all'entità
                ObiettivoPrevenzioneCorruzioneTrasparenza entity = mapper.toEntity(request, context);

                // Impostazione delle relazioni usando getReferenceById
                entity.setSezione23(sezione23Repository.getReferenceById(request.getIdSezione23()));

                // Relazioni opzionali: possono essere null
                if (request.getIdOVP() != null) {
                    entity.setOvp(ovpRepository.getReferenceById(request.getIdOVP()));
                }
                if (request.getIdStrategiaOVP() != null) {
                    entity.setOvpStrategia(ovpStrategiaRepository.getReferenceById(request.getIdStrategiaOVP()));
                }
                if (request.getIdObbiettivoPerformance() != null) {
                    entity.setObbiettivoPerformance(obbiettivoPerformanceRepository.getReferenceById(request.getIdObbiettivoPerformance()));
                }

                // Sincronizzazione Indicatori (OneToMany)
                syncIndicatori(entity, request.getIndicatori());

                entitiesToSave.add(entity);
            }

            // Salvataggio batch di tutte le entità
            List<ObiettivoPrevenzioneCorruzioneTrasparenza> savedEntities =
                    obiettivoPrevenzioneCorruzioneTrasparenzaRepository.saveAll(entitiesToSave);


        } catch (Exception e) {
            log.error("Errore durante saveAll ObiettivoPrevenzioneCorruzioneTrasparenza: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch degli ObiettivoPrevenzioneCorruzioneTrasparenza", e);
        }


    }

    @Override
    @Transactional(readOnly = true)
    public List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> getAllBySezione23(Long idSezione23) {
        if (idSezione23 == null) {
            throw new IllegalArgumentException("L'ID della Sezione23 non può essere nullo");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        // Recupero il riferimento alla Sezione23
        Sezione23 sezione23 = sezione23Repository.getReferenceById(idSezione23);

        // Recupero tutte le entity ObiettivoPrevenzioneCorruzioneTrasparenza associate a questa Sezione23
        List<ObiettivoPrevenzioneCorruzioneTrasparenza> entities =
                obiettivoPrevenzioneCorruzioneTrasparenzaRepository.getObiettivoPrevenzioneCorruzioneTrasparenzaFindBySezione23(sezione23);

        if (entities.isEmpty()) {
            return new ArrayList<>();
        }

        // Mapping diretto senza caricare collezioni extra
        return entities.stream()
                .map(entity -> mapper.toDto(entity, context))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID dell'ObiettivoPrevenzioneCorruzioneTrasparenza non può essere nullo");
        }
        if (!obiettivoPrevenzioneCorruzioneTrasparenzaRepository.existsById(id)) {
            throw new IllegalArgumentException("ObiettivoPrevenzioneCorruzioneTrasparenza non trovato: " + id);
        }
        try {
            log.info("Eliminazione ObiettivoPrevenzioneCorruzioneTrasparenza con ID={}", id);

            // Recupera l'entità prima della cancellazione
            ObiettivoPrevenzioneCorruzioneTrasparenza entity = obiettivoPrevenzioneCorruzioneTrasparenzaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ObiettivoPrevenzioneCorruzioneTrasparenza non trovato con id: " + id));

            // *** STEP 1: Imposta a NULL il riferimento obiettivoPrevenzioneCorruzioneTrasparenza nelle MisuraPrevenzioneEventoRischio collegate ***
            int misurePrevenzioneAggiornate = misuraPrevenzioneEventoRischioRepository
                .setObiettivoPrevenzioneCorruzioneTrasparenzaToNullByObiettivoId(id);

            if (misurePrevenzioneAggiornate > 0) {
                log.info("Impostato a NULL il riferimento obiettivoPrevenzioneCorruzioneTrasparenza in {} MisuraPrevenzioneEventoRischio",
                    misurePrevenzioneAggiornate);

                // *** STEP 2: Cambio stato Sezione23 a IN_COMPILAZIONE ***
                log.warn("ObiettivoPrevenzioneCorruzioneTrasparenza ID={} aveva {} misure prevenzione evento rischio collegate. Cambio stato Sezione23 a IN_COMPILAZIONE",
                    id, misurePrevenzioneAggiornate);

                // Recupera la Sezione23 dall'entità
                if (entity.getSezione23() != null) {
                    try {
                        Sezione23 sezione23 = entity.getSezione23();

                        // Cambia lo stato della Sezione23 a IN_COMPILAZIONE
                        it.ey.enums.StatoEnum nuovoStato = it.ey.enums.StatoEnum.IN_COMPILAZIONE;
                        sezione23.setIdStato(nuovoStato.getId());
                        sezione23Repository.save(sezione23);

                        // Salva lo storico stato (solo se lo stato è cambiato)
                        String statoCorrente = it.ey.utils.StoricoStatoSezioneUtils.getStato(
                            storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(
                                sezione23.getId(),
                                it.ey.enums.Sezione.SEZIONE_23.name()
                            )
                        );

                        if (!nuovoStato.name().equals(statoCorrente)) {
                            storicoStatoSezioneRepository.save(
                                StoricoStatoSezione.builder()
                                    .statoSezione(
                                        StatoSezione.builder()
                                            .id(nuovoStato.getId())
                                            .testo(nuovoStato.getDescrizione())
                                            .build()
                                    )
                                    .idEntitaFK(sezione23.getId())
                                    .codTipologiaFK(it.ey.enums.Sezione.SEZIONE_23.name())
                                    .testo(nuovoStato.getDescrizione())
                                    .build()
                            );
                            log.info("Stato Sezione23 id={} cambiato a IN_COMPILAZIONE a causa della cancellazione dell'ObiettivoPrevenzioneCorruzioneTrasparenza id={}",
                                sezione23.getId(), id);
                        }
                    } catch (Exception e) {
                        log.error("Errore durante il cambio stato Sezione23 per ObiettivoPrevenzioneCorruzioneTrasparenza ID={}: {}", id, e.getMessage());
                        // Non blocco la cancellazione per errori nel cambio stato
                    }
                }
            }

            // *** STEP 3: Cancellazione dell'entità ***
            obiettivoPrevenzioneCorruzioneTrasparenzaRepository.deleteById(id);
            log.info("ObiettivoPrevenzioneCorruzioneTrasparenza con id={} cancellato con successo", id);

        }  catch (Exception ex) {
            log.error("Errore durante l'eliminazione dell'ObiettivoPrevenzioneCorruzioneTrasparenza ID={}: {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Errore durante l'eliminazione dell'ObiettivoPrevenzioneCorruzioneTrasparenza. Id: " + id, ex);
        }
    }

    /**
     * Sincronizza la lista degli indicatori con l'entità padre.
     * Questo metodo gestisce la creazione, aggiornamento e rimozione degli indicatori.
     */
    private void syncIndicatori(ObiettivoPrevenzioneCorruzioneTrasparenza parent,
                                 List<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setIndicatori(new ArrayList<>());
            return;
        }

        List<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori> entities = new ArrayList<>();

        for (ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO dto : dtoList) {
            // Controllo null safety
            if (dto == null || dto.getIndicatore() == null || dto.getIndicatore().getId() == null) {
                log.warn("IndicatoreDTO nullo o senza ID in ObiettivoPrevenzioneCorruzioneTrasparenza, skip");
                continue;
            }

            // Usa builder per creare l'entità con ID
            ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori entity = ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori.builder()
                .id(dto.getId())
                .obiettivoPrevenzioneCorruzioneTrasparenza(parent)
                .indicatore(indicatoreRepository.getReferenceById(dto.getIndicatore().getId()))
                .build();
            entities.add(entity);
        }

        parent.setIndicatori(entities);
    }
}
