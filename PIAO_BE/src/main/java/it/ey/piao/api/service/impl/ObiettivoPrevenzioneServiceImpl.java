package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.MisuraPrevenzioneMapper;
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
    public ObiettivoPrevenzioneDTO saveOrUpdate(ObiettivoPrevenzioneDTO obiettivoPrevenzione) {
        if (obiettivoPrevenzione == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        try {
            // Otteniamo la sezione di riferimento
            Sezione23 sezione = sezione23Repository.getReferenceById(obiettivoPrevenzione.getIdSezione23());

            // Se l'ID è presente aggiorniamo, altrimenti creiamo nuovo
            ObiettivoPrevenzione entity;
            if (obiettivoPrevenzione.getId() != null) {
                entity = obiettivoPrevenzioneRepository.findById(obiettivoPrevenzione.getId())
                    .orElse(new ObiettivoPrevenzione());
            } else {
                entity = new ObiettivoPrevenzione();
            }

            // Copiamo i campi "semplici" dal DTO all'entity
            entity.setDenominazione(obiettivoPrevenzione.getDenominazione());
            entity.setDescrizione(obiettivoPrevenzione.getDescrizione());
            entity.setCodice(obiettivoPrevenzione.getCodice());
            entity.setSezione23(sezione);

            // Sincronizziamo la lista di indicatori
            syncIndicatori(entity, obiettivoPrevenzione.getIndicatori());

            // Salviamo l'entity
            ObiettivoPrevenzione saved = obiettivoPrevenzioneRepository.save(entity);


            return obiettivoPrevenzioneMapper.toDto(saved, new CycleAvoidingMappingContext());

        } catch (Exception e) {
            log.error("Errore saveOrUpdate ObiettivoPrevenzione id={}: {}", obiettivoPrevenzione.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate ObiettivoPrevenzione", e);
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
            // eliminiamo la corrispondenza trovata
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
        List<ObiettivoPrevenzioneIndicatore> managedList = parent.getIndicatori();


        // se è null la creiamo
        if (managedList == null) {
            managedList = new ArrayList<>();
            parent.setIndicatori(managedList);
        }

        // rimuovi tutti gli elementi non più presenti
        managedList.clear();

        // se abbiamo nuovi indicatori
        if (dtoList != null && !dtoList.isEmpty()) {
            // iteriamo ogni DTO ricevuto dal frontend
            for (ObiettivoPrevenzioneIndicatoreDTO indDTO : dtoList) {
                // creiamo un nuovo entity e con il builder associamo i campi
                ObiettivoPrevenzioneIndicatore entity = ObiettivoPrevenzioneIndicatore.builder()
                    .id(indDTO.getId())
                    .obiettivoPrevenzione(parent)
                    .indicatore(indicatoreRepository.getReferenceById(indDTO.getIndicatore().getId()))
                    .build();
                // alla
                managedList.add(entity);
            }
        }
    }


}
