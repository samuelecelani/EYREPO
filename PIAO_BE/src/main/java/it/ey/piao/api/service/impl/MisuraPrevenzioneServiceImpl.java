package it.ey.piao.api.service.impl;


import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.MisuraPrevenzioneMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IMisuraPrevenzioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MisuraPrevenzioneServiceImpl implements IMisuraPrevenzioneService {

    private final IMisuraPrevenzioneRepository misuraPrevenzioneRepository;
    private final MisuraPrevenzioneMapper misuraPrevenzioneMapper;
    private final IObiettivoPrevenzioneRepository obiettivoPrevenzioneRepository;
    private final IIndicatoreRepository indicatoreRepository;
    private final IStakeHolderRepository stakeHolderRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final CommonMapper commonMapper;
    private final ISezione23Repository sezione23Repository;

    private static final Logger log = LoggerFactory.getLogger(MisuraPrevenzioneServiceImpl.class);


    public MisuraPrevenzioneServiceImpl(IMisuraPrevenzioneRepository misuraPrevenzioneRepository,
                                        MisuraPrevenzioneMapper misuraPrevenzioneMapper,
                                        IObiettivoPrevenzioneRepository obiettivoPrevenzioneRepository,
                                        IIndicatoreRepository indicatoreRepository,
                                        IStakeHolderRepository stakeHolderRepository,
                                        IUlterioriInfoRepository ulterioriInfoRepository,
                                        CommonMapper commonMapper, ISezione23Repository sezione23Repository) {
        this.misuraPrevenzioneRepository = misuraPrevenzioneRepository;
        this.misuraPrevenzioneMapper = misuraPrevenzioneMapper;
        this.obiettivoPrevenzioneRepository = obiettivoPrevenzioneRepository;
        this.indicatoreRepository = indicatoreRepository;
        this.stakeHolderRepository = stakeHolderRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.commonMapper = commonMapper;
        this.sezione23Repository = sezione23Repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MisuraPrevenzioneDTO saveOrUpdate(MisuraPrevenzioneDTO misuraPrevenzione) {
        if (misuraPrevenzione == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        try {
           // Verifica che l'obiettivo di riferimento esista
            ObiettivoPrevenzione obiettivo = obiettivoPrevenzioneRepository
                .findById(misuraPrevenzione.getIdObiettivoPrevenzione())
                .orElseThrow(() -> new IllegalArgumentException(
                    "ObiettivoPrevenzione non trovato id=" + misuraPrevenzione.getIdObiettivoPrevenzione()
                ));
            // Se l'ID è presente aggiorno, altrimenti creiamo nuovo
            MisuraPrevenzione entity;
            if (misuraPrevenzione.getId() != null) {
                entity = misuraPrevenzioneRepository.findById(misuraPrevenzione.getId())
                    .orElse(new MisuraPrevenzione());
            } else {
                entity = new MisuraPrevenzione();
            }
            // Copioi campi "semplici" dal DTO all'entity
            entity.setDescrizione(misuraPrevenzione.getDescrizione());
            entity.setDenominazione(misuraPrevenzione.getDenominazione());
            entity.setObiettivoPrevenzione(obiettivo);

            // Imposto la Sezione23
            if (misuraPrevenzione.getIdSezione23() != null) {
                Sezione23 sezione23 = sezione23Repository.getReferenceById(misuraPrevenzione.getIdSezione23());
                entity.setSezione23(sezione23);
            }

            // Sincrono la lista di indicatori
            syncIndicatori(entity, misuraPrevenzione.getIndicatori());
            // Sincrono la lista di stakeholders
            syncStakeHolders(entity, misuraPrevenzione.getStakeholder());

            MisuraPrevenzione saved= misuraPrevenzioneRepository.save(entity);
            //Creo il DTO da restituire e popolare indicatori
            return misuraPrevenzioneMapper.toDto(saved,new CycleAvoidingMappingContext());

        } catch (Exception e) {
            log.error("Errore saveOrUpdate MisuraPrevenzione id={}: {}", misuraPrevenzione.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate MisuraPrevenzione", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<MisuraPrevenzioneDTO> getAllByObiettivoPrevenzione(Long idObiettivoPrevenzione) {
        if (idObiettivoPrevenzione == null) {
            throw new IllegalArgumentException("L'ID dell'ObiettivoPrevenzione non può essere nullo");
        }
        // getReferenceById è un metodo di JPA che restituisce un proxy
        try{
            ObiettivoPrevenzione obiettivo = obiettivoPrevenzioneRepository.getReferenceById(idObiettivoPrevenzione);


            // Recuperiamo tutte le misure di quell'obiettivo
            List<MisuraPrevenzione> entities = misuraPrevenzioneRepository.getMisuraPrevenzioneByObiettivoPrevenzione(obiettivo);

            // prese le misure iniziamo uno stream per mapparle in dto
            return entities.stream().map(entity -> {

                return misuraPrevenzioneMapper.toDto(entity,new CycleAvoidingMappingContext());
                }).toList();


        } catch (Exception e) {
            log.error("Errore getAllByObiettivoPrevenzione id={} : {}", idObiettivoPrevenzione, e.getMessage(), e);
            throw new RuntimeException("Errore recupero Misure per ObiettivoPrevenzione", e);
        }




    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<MisuraPrevenzioneDTO> getAllBySezione23(Long idSezione23) {


        // Controllo se la sezione esiste
        if (idSezione23 == null) {
            throw new IllegalArgumentException("L'ID della Sezione23 non può essere nullo");
        }

        try {
            // getReferenceById è un metodo di JPA che restituisce un proxy

            Sezione23 sezione23 = sezione23Repository.getReferenceById(idSezione23);

            // Recuperiamo tutti gli obiettivi che sono genitori di Misura
            List<ObiettivoPrevenzione> obiettivi = obiettivoPrevenzioneRepository.getObiettivoPrevenzioneBySezione23(sezione23);

            // creo la lista delle misure
            List<MisuraPrevenzioneDTO> misure = new ArrayList<>();

            for (ObiettivoPrevenzione obiettivo : obiettivi) {
                // per ogni obiettivo recuperiamo le misure e le aggiungiamo alla lista totale
                // ma lo faccio richiamando il metodo gia esistente byObiettivo
                misure.addAll(getAllByObiettivoPrevenzione(obiettivo.getId()));
            }

            return misure;

        } catch (Exception e) {
            log.error("Errore getAllBySezione23 id={} : {}", idSezione23, e.getMessage(), e);
            throw new RuntimeException("Errore recupero Misure per Sezione23", e);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID della MisuraPrevenzione non può essere nullo");
        }

        try {
            // passato l'id lo cerchiamo e salviamo la corrispondenza come entity
            // se non trova nulla lanciamo l'errore runtime
            MisuraPrevenzione misura = misuraPrevenzioneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "MisuraPrevenzione non trovata con id=" + id
                ));

            // eliminiamo la corrispondenza trovata
            misuraPrevenzioneRepository.delete(misura);

            log.info("MisuraPrevenzione cancellata con id={}", id);

            // se qualcosa fallisce durante il processo di eliminazione lo vediamo e c'è il rollback

        } catch (Exception e) {
            log.error("Errore deleteById MisuraPrevenzione id={} : {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore cancellazione MisuraPrevenzione", e);
        }

    }

    @Override
    public MisuraPrevenzioneDTO enrichWithRelations(MisuraPrevenzione entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Il DTO e l'ID non possono essere nulli");
        }

        try {
            MisuraPrevenzioneDTO dto = misuraPrevenzioneMapper.toDto(entity,new CycleAvoidingMappingContext());

            log.debug("MisuraPrevenzioneDTO con id={} arricchito con relazioni", entity.getId());
            return dto;
        } catch (Exception e) {
            log.error("Errore durante l'arricchimento dell'MisuraPrevenzioneDTO con id={}: {}",
                entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'arricchimento dell'MisuraPrevenzioneDTO con le relazioni", e);
        }

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,readOnly = true)
    public void loadMongoDataForMisura(MisuraPrevenzioneDTO misuraDTO) {
        if (misuraDTO == null || misuraDTO.getId() == null) {
            log.warn("MisuraPrevenzioneDTO o ID è null, skip caricamento MongoDB");
            return;
        }

        try {
            // Carica i dati MongoDB per ogni Indicatore della misura
            if (misuraDTO.getIndicatori() != null && !misuraDTO.getIndicatori().isEmpty()) {
                misuraDTO.getIndicatori().forEach(indDTO -> {
                    if (indDTO.getIndicatore() != null && indDTO.getIndicatore().getId() != null) {
                        // Carica UlterioriInfo MongoDB per l'indicatore
                        UlterioriInfoDTO ulterioriInfo = Optional.ofNullable(
                                ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                                    indDTO.getIndicatore().getId(),
                                    Sezione.SEZIONE_23
                                )
                            )
                            .map(u -> commonMapper.ulterioriInfoEntityToDto(u,new CycleAvoidingMappingContext()))
                            .orElse(null);
                        indDTO.getIndicatore().setAddInfo(ulterioriInfo);
                    }
                });
            }

            log.debug("Dati MongoDB caricati per MisuraPrevenzione id={}", misuraDTO.getId());
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per MisuraPrevenzione id={}: {}",
                misuraDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB MisuraPrevenzione", e);
        }
    }



    private void syncIndicatori(MisuraPrevenzione parent, List<MisuraPrevenzioneIndicatoreDTO> dtoList) {
        // prendiamo la lista gestita dall'entity
        List<MisuraPrevenzioneIndicatore> managedList = parent.getIndicatori();

        // se è null la creiamo
        if (managedList == null) {
            managedList = new ArrayList<>();
            parent.setIndicatori(managedList);
        }

        // Rimuove tutti gli elementi non più presenti
        managedList.clear();


        // se abbiamo nuovi indicatori
        if (dtoList != null && !dtoList.isEmpty()) {
            // iteriamo ogni DTO ricevuto dal frontend
            for (MisuraPrevenzioneIndicatoreDTO dto : dtoList) {
                // creiamo un nuovo entity e con il builder associamo i campi
                MisuraPrevenzioneIndicatore entity = MisuraPrevenzioneIndicatore.builder()
                    .id(dto.getId())
                    .misuraPrevenzione(parent)
                    .indicatore(indicatoreRepository.getReferenceById(dto.getIndicatore().getId()))
                    .build();
                managedList.add(entity);
            }
        }
    }

    /**
     * Sincronizza la lista di StakeHolders della MisuraPrevenzione.
     * Gli stakeholder esistono già nel DB, va solo settato il riferimento alla misura.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni grazie a orphanRemoval=true.
     */
    private void syncStakeHolders(MisuraPrevenzione parent, List<MisuraPrevenzioneStakeholderDTO> dtoList) {
        // prendiamo la lista gestita dall'entity
        List<MisuraPrevenzioneStakeholder> managedList = parent.getStakeholder();

        // se è null la creiamo
        if (managedList == null) {
            managedList = new ArrayList<>();
            parent.setStakeholder(managedList);
        }

        // Rimuove tutti gli elementi non più presenti
        managedList.clear();

        // se abbiamo nuovi stakeholders
        if (dtoList != null && !dtoList.isEmpty()) {
            // iteriamo ogni DTO ricevuto dal frontend
            for (MisuraPrevenzioneStakeholderDTO dto : dtoList) {
                // creiamo un nuovo entity e con il builder associamo i campi
                MisuraPrevenzioneStakeholder entity = MisuraPrevenzioneStakeholder.builder()
                    .id(dto.getId())
                    .misuraPrevenzione(parent)
                    .stakeholder(stakeHolderRepository.getReferenceById(dto.getStakeholder().getId()))
                    .build();
                managedList.add(entity);
            }
        }
    }

}
