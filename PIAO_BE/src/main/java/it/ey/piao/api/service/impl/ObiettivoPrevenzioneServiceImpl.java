package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.MisuraPrevenzioneMapper;
import it.ey.piao.api.mapper.ObbiettivoPerformanceMapper;
import it.ey.piao.api.mapper.ObiettivoPrevenzioneMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IIndicatoreRepository;
import it.ey.piao.api.repository.IObiettivoPrevenzioneRepository;
import it.ey.piao.api.repository.ISezione23Repository;
import it.ey.piao.api.service.IMisuraPrevenzioneService;
import it.ey.piao.api.service.IObiettivoPrevenzioneService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ObiettivoPrevenzioneServiceImpl implements IObiettivoPrevenzioneService {
    private final IMisuraPrevenzioneService misuraPrevenzioneService;
    private final IObiettivoPrevenzioneRepository obiettivoPrevenzioneRepository;
    private final IIndicatoreRepository indicatoreRepository;
    private final ISezione23Repository sezione23Repository;
    private final ObiettivoPrevenzioneMapper obiettivoPrevenzioneMapper;
    private final CommonMapper commonMapper;
    private final MisuraPrevenzioneMapper misuraPrevenzioneMapper;

    private static final Logger log = LoggerFactory.getLogger(ObiettivoPrevenzioneServiceImpl.class);


    public ObiettivoPrevenzioneServiceImpl(IMisuraPrevenzioneService misuraPrevenzioneService, IObiettivoPrevenzioneRepository obiettivoPrevenzioneRepository, ISezione23Repository sezione23Repository, ObiettivoPrevenzioneMapper obiettivoPrevenzioneMapper, CommonMapper commonMapper, IIndicatoreRepository indicatoreRepository, MisuraPrevenzioneMapper misuraPrevenzioneMapper) {
        this.misuraPrevenzioneService = misuraPrevenzioneService;
        this.obiettivoPrevenzioneRepository = obiettivoPrevenzioneRepository;
        this.sezione23Repository = sezione23Repository;
        this.obiettivoPrevenzioneMapper = obiettivoPrevenzioneMapper;
        this.commonMapper = commonMapper;
        this.indicatoreRepository=indicatoreRepository;
        this.misuraPrevenzioneMapper = misuraPrevenzioneMapper;
    }

    @Override
    public ObiettivoPrevenzioneDTO  saveOrUpdate(ObiettivoPrevenzioneDTO obiettivoPrevenzione) {
        if (obiettivoPrevenzione == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        try {

            // Se l'ID è presente aggiorniamo, altrimenti creiamo nuovo
         ObiettivoPrevenzione entity = obiettivoPrevenzioneMapper.toEntity(obiettivoPrevenzione,new CycleAvoidingMappingContext());
            // Sincronizziamo la lista di indicatori
            syncIndicatori(entity, obiettivoPrevenzione.getIndicatori());
            entity.setSezione23( sezione23Repository.getReferenceById(obiettivoPrevenzione.getIdSezione23()));

            // Salviamo l'entity
          return obiettivoPrevenzioneMapper.toDto( obiettivoPrevenzioneRepository.save(entity), new CycleAvoidingMappingContext());

        } catch (Exception e) {
            log.error("Errore saveOrUpdate ObiettivoPrevenzione id={}: {}", obiettivoPrevenzione.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate ObiettivoPrevenzione", e);
        }
    }

    @Override
    public void  saveAll(List<ObiettivoPrevenzioneDTO> obiettiviPrevenzione) {
        if (obiettiviPrevenzione == null || obiettiviPrevenzione.isEmpty()) {
            throw new IllegalArgumentException("La lista non può essere nulla o vuota");
        }

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            // Converto tutti i DTO in Entity
            List<ObiettivoPrevenzione> entitiesToSave = new ArrayList<>();

            for (ObiettivoPrevenzioneDTO dto : obiettiviPrevenzione) {
                if (dto == null || dto.getId() == null) {
                    log.warn("ObiettivoPrevenzioneDTO nullo nella lista, skip");
                        continue;
                }

                // Mappo DTO -> Entity
                ObiettivoPrevenzione entity = obiettivoPrevenzioneMapper.toEntity(dto, context);

                // Sincronizziamo la lista di indicatori
                syncIndicatori(entity, dto.getIndicatori());

                // Imposto la relazione con Sezione23
                entity.setSezione23(sezione23Repository.getReferenceById(dto.getIdSezione23()));

                entitiesToSave.add(entity);
            }

            // Salvataggio batch con saveAll di Hibernate
            List<ObiettivoPrevenzione> savedEntities = obiettivoPrevenzioneRepository.saveAll(entitiesToSave);

            // *** IMPORTANTE: Aggiorna gli ID nei DTO dopo il salvataggio ***
            // Questo è necessario perché le MisuraPrevenzione referenziano gli ObiettivoPrevenzione
            // e hanno bisogno degli ID aggiornati
            savedEntities.forEach(saved -> {
                obiettiviPrevenzione.stream()
                    .filter(dto -> dto != null &&
                           ((dto.getId() == null && saved.getId() != null) ||
                            (dto.getId() != null && dto.getId().equals(saved.getId()))))
                    .findFirst()
                    .ifPresent(dto -> {
                        dto.setId(saved.getId());
                        log.debug("Aggiornato ID ObiettivoPrevenzione nel DTO: {}", saved.getId());
                    });
            });

            log.info("SaveAll ObiettivoPrevenzione completato: {} elementi salvati", savedEntities.size());


        } catch (Exception e) {
            log.error("Errore saveAll ObiettivoPrevenzione: {}", e.getMessage(), e);
            throw new RuntimeException("Errore saveAll ObiettivoPrevenzione", e);
        }
    }


    @Override
    public List<ObiettivoPrevenzioneDTO> getAllBySezione23(Long idSezione23) {

        // evito il NullPointerException
        if (idSezione23 == null) {
            throw new IllegalArgumentException("L'ID della Sezione23 non può essere nullo");
        }

        // getReferenceById è un metodo di JPA che restituisce un proxy
        try {
            Sezione23 sezione23=sezione23Repository.getReferenceById(idSezione23);

            // in una lista salviamo gli obiettivi che ci vengono restituiti al caricamento della sezione
            // e li restituiamo mappandoli in DTTO
            List<ObiettivoPrevenzione> entities =obiettivoPrevenzioneRepository.getObiettivoPrevenzioneBySezione23(sezione23);
            return entities.stream()
                .map(entity -> {
                    return obiettivoPrevenzioneMapper.toDto(entity, new CycleAvoidingMappingContext());
                })
                .toList();


        } catch (Exception e) {
            log.error("Errore getObiettivoPrevenzioneBySezione23 id={} :{} ",idSezione23,e.getMessage(),e);
            throw new RuntimeException("Errore recupero ObiettivoPrevenzione per Sezione23", e);
        }

    }

    @Override
    public void deleteById(Long id) {
        // evito il NullPointerException
        if (id == null) {
            throw new IllegalArgumentException("L'ID non può essere nullo");
        }
         // passato l'id lo cerchiamo e salviamo la corrispondenza come entity
        // se non trova nulla lanciamo l'errore runtime
        try {
            ObiettivoPrevenzione entity =
                obiettivoPrevenzioneRepository.findById(id)
                    .orElseThrow(() ->
                        new RuntimeException("ObiettivoPrevenzione non trovato con id: " + id)
                    );

            // *** STEP 1: Imposta a NULL il riferimento obiettivoPrevenzione nelle MisuraPrevenzione collegate ***
            int misureAggiornate = misuraPrevenzioneService.setObiettivoPrevenzioneToNullByObiettivoId(id);
            log.info("MisuraPrevenzione sganciate dall'ObiettivoPrevenzione id={}: {} misure aggiornate", id, misureAggiornate);

            // *** STEP 2: Elimina l'ObiettivoPrevenzione (le MisuraPrevenzione NON vengono cancellate) ***
            obiettivoPrevenzioneRepository.delete(entity);

            log.info("ObiettivoPrevenzione con id={} cancellato con successo", id);


            // se qualcosa fallisce durante il processo di eliminazione lo vediamo e c'è il rollback
        } catch (Exception e) {
            log.error("Errore delete ObiettivoPrevenzione id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore eliminazione ObiettivoPrevenzione", e);
        }


    }

    @Override
    public ObiettivoPrevenzioneDTO enrichWithRelations(ObiettivoPrevenzione entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Il DTO e l'ID non possono essere nulli");
        }
        try {
            // Mappa entity -> DTO
            ObiettivoPrevenzioneDTO dto = obiettivoPrevenzioneMapper.toDto(entity, new CycleAvoidingMappingContext());
            // Recupero le misure legate all'obiettivo
            List<MisuraPrevenzioneDTO> misure = misuraPrevenzioneService.getAllByObiettivoPrevenzione(entity.getId())
                .stream()
                .map(m -> misuraPrevenzioneService.enrichWithRelations(
                    misuraPrevenzioneMapper.toEntity(m, new CycleAvoidingMappingContext())
                ))
                .toList();

            dto.setMisurePrevenzione(misure);

            log.debug("ObiettivoPrevenzioneDTO con id={} arricchito con relazioni", entity.getId());
            return dto;
        } catch (Exception e) {
            log.error("Errore durante l'arricchimento dell'ObiettivoPrevenzioneDTO con id={}: {}",
                entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'arricchimento dell'ObiettivoPrevenzioneDTO con le relazioni", e);
        }

    }

    @Override
    public void loadMongoDataForObiettivo(ObiettivoPrevenzioneDTO obiettivoDTO) {
        if (obiettivoDTO == null || obiettivoDTO.getId() == null) {
            log.warn("ObiettivoPrevenzioneDTO o ID è null, skip caricamento MongoDB");
            return;
        }

        try {
            // ObiettivoPrevenzione al momento non ha campi MongoDB diretti
            // Se in futuro verranno aggiunti (es. UlterioriInfo), caricarli qui

            log.debug("Dati MongoDB caricati per ObiettivoPrevenzione id={}", obiettivoDTO.getId());
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per ObiettivoPrevenzione id={}: {}",
                obiettivoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB ObiettivoPrevenzione", e);
        }
    }

    // --- SYNC (POST/PUT) ---
    private void syncIndicatori(ObiettivoPrevenzione parent, List<ObiettivoPrevenzioneIndicatoreDTO> dtoList) {
        // prendiamo la lista gestita dall'entity
            if (dtoList == null || dtoList.isEmpty()) {
                parent.setIndicatori(new ArrayList<>());
                return;
            }
            List<ObiettivoPrevenzioneIndicatore> entities = new ArrayList<>();

            for (ObiettivoPrevenzioneIndicatoreDTO dto : dtoList) {
                ObiettivoPrevenzioneIndicatore entity = ObiettivoPrevenzioneIndicatore.builder()
                    .id(dto.getId())
                    .obiettivoPrevenzione(parent)
                    .indicatore(indicatoreRepository.getReferenceById(dto.getIndicatore().getId()))
                    .build();
                entities.add(entity);
            }

            parent.setIndicatori(entities);
        }


}
