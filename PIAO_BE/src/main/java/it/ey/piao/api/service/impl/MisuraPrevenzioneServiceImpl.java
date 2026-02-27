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
    public void  saveOrUpdate(MisuraPrevenzioneDTO misuraPrevenzione) {
        if (misuraPrevenzione == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        try {


            MisuraPrevenzione entity = misuraPrevenzioneMapper.toEntity(misuraPrevenzione, new CycleAvoidingMappingContext());
            // Imposto la Sezione23
            if (misuraPrevenzione.getIdSezione23() != null) {
                entity.setSezione23(sezione23Repository.getReferenceById(misuraPrevenzione.getIdSezione23()));
            }
            // Sincrono la lista di indicatori
            syncIndicatori(entity, misuraPrevenzione.getIndicatori());
            // Sincrono la lista di stakeholders
            syncStakeHolders(entity, misuraPrevenzione.getStakeholder());

          misuraPrevenzioneRepository.save(entity);


        } catch (Exception e) {
            log.error("Errore saveOrUpdate MisuraPrevenzione id={}: {}", misuraPrevenzione.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate MisuraPrevenzione", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void  saveAll(List<MisuraPrevenzioneDTO> misurePrevenzione) {
        if (misurePrevenzione == null || misurePrevenzione.isEmpty()) {
            throw new IllegalArgumentException("La lista non può essere nulla o vuota");
        }

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            // Converto tutti i DTO in Entity
            List<MisuraPrevenzione> entitiesToSave = new ArrayList<>();

            for (MisuraPrevenzioneDTO dto : misurePrevenzione) {
                if (dto == null) {
                    log.warn("MisuraPrevenzioneDTO nullo nella lista, skip");
                    continue;
                }

                log.debug("Elaborazione MisuraPrevenzione id={}, idObiettivoPrevenzione={}, idSezione23={}",
                    dto.getId(), dto.getIdObiettivoPrevenzione(), dto.getIdSezione23());

                // Mappo DTO -> Entity
                MisuraPrevenzione entity = misuraPrevenzioneMapper.toEntity(dto, context);

                // Sincronizziamo la lista di indicatori
                syncIndicatori(entity, dto.getIndicatori());

                // Sincronizziamo la lista di stakeholders
                syncStakeHolders(entity, dto.getStakeholder());

                // Imposto la relazione con ObiettivoPrevenzione SOLO se esiste nel DB
                if(dto.getIdObiettivoPrevenzione() != null) {
                        entity.setObiettivoPrevenzione(obiettivoPrevenzioneRepository.getReferenceById(dto.getIdObiettivoPrevenzione()));

                    }
                if(dto.getIdSezione23() != null) {
                    // Imposto la relazione con Sezione23
                    entity.setSezione23(sezione23Repository.getReferenceById(dto.getIdSezione23()));
                    log.debug("Associato Sezione23 id={} a MisuraPrevenzione", dto.getIdSezione23());
                }

                entitiesToSave.add(entity);
            }

            log.info("Salvando {} MisuraPrevenzione...", entitiesToSave.size());

            // Salvataggio batch con saveAll di Hibernate
            List<MisuraPrevenzione> savedEntities = misuraPrevenzioneRepository.saveAll(entitiesToSave);

            log.info("SaveAll MisuraPrevenzione completato: {} elementi salvati", savedEntities.size());

        } catch (Exception e) {
            log.error("Errore saveAll MisuraPrevenzione: {} - Causa: {}", e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "N/A", e);
            // Log dello stacktrace completo per debug
            log.error("StackTrace completo:", e);
            throw new RuntimeException("Errore saveAll MisuraPrevenzione: " + e.getMessage(), e);
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
    @Transactional(propagation = Propagation.REQUIRED)
    public int setObiettivoPrevenzioneToNullByObiettivoId(Long idObiettivo) {
        if (idObiettivo == null) {
            throw new IllegalArgumentException("L'ID dell'ObiettivoPrevenzione non può essere nullo");
        }
        int updated = misuraPrevenzioneRepository.setObiettivoPrevenzioneToNullByObiettivoId(idObiettivo);
        log.info("MisuraPrevenzione sganciate dall'ObiettivoPrevenzione id={}: {} misure aggiornate", idObiettivo, updated);
        return updated;
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
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setIndicatori(new ArrayList<>());
            return;
        }

        List<MisuraPrevenzioneIndicatore> entities = new ArrayList<>();

        // iteriamo ogni DTO ricevuto dal frontend
        for (MisuraPrevenzioneIndicatoreDTO dto : dtoList) {
            // Controllo null safety
            if (dto == null || dto.getIndicatore() == null || dto.getIndicatore().getId() == null) {
                log.warn("IndicatoreDTO nullo o senza ID nella MisuraPrevenzione, skip");
                continue;
            }

            // creiamo un nuovo entity e con il builder associamo i campi
            MisuraPrevenzioneIndicatore entity = MisuraPrevenzioneIndicatore.builder()
                .id(dto.getId())
                .misuraPrevenzione(parent)
                .indicatore(indicatoreRepository.getReferenceById(dto.getIndicatore().getId()))
                .build();
            entities.add(entity);
        }

        parent.setIndicatori(entities);
    }


    /**
     * Sincronizza la lista di StakeHolders della MisuraPrevenzione.
     * Gli stakeholder esistono già nel DB, va solo settato il riferimento alla misura.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni grazie a orphanRemoval=true.
     */
    private void syncStakeHolders(MisuraPrevenzione parent, List<MisuraPrevenzioneStakeholderDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setStakeholder(new ArrayList<>());
            return;
        }

        List<MisuraPrevenzioneStakeholder> entities = new ArrayList<>();

        // iteriamo ogni DTO ricevuto dal frontend
        for (MisuraPrevenzioneStakeholderDTO dto : dtoList) {
            // Controllo null safety
            if (dto == null || dto.getStakeholder() == null || dto.getStakeholder().getId() == null) {
                log.warn("StakeholderDTO nullo o senza ID nella MisuraPrevenzione, skip");
                continue;
            }

            // creiamo un nuovo entity e con il builder associamo i campi
            MisuraPrevenzioneStakeholder entity = MisuraPrevenzioneStakeholder.builder()
                .id(dto.getId())
                .misuraPrevenzione(parent)
                .stakeholder(stakeHolderRepository.getReferenceById(dto.getStakeholder().getId()))
                .build();
            entities.add(entity);
        }

        parent.setStakeholder(entities);
    }

}
