package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.DatiPubblicatiMapper;
import it.ey.piao.api.mapper.ObbligoLeggeMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IObbligoLeggeRepository;
import it.ey.piao.api.repository.ISezione23Repository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IDatiPubblicatiService;
import it.ey.piao.api.service.IObbligoLeggeService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ObbligoLeggeServiceImpl implements IObbligoLeggeService {

    private final ISezione23Repository sezione23Repository;
    private final MongoUtils mongoUtils;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DatiPubblicatiMapper datiPubblicatiMapper;
    private final IDatiPubblicatiService datiPubblicatiService;

    private final ObbligoLeggeMapper obbligoLeggeMapper;
    private final CommonMapper commonMapper;

    private final IObbligoLeggeRepository obbligoLeggeRepository;

    private static final Logger log = LoggerFactory.getLogger(ObbligoLeggeServiceImpl.class);

    public ObbligoLeggeServiceImpl(ISezione23Repository sezione23Repository, MongoUtils mongoUtils, IUlterioriInfoRepository ulterioriInfoRepository, ApplicationEventPublisher eventPublisher, DatiPubblicatiMapper datiPubblicatiMapper, IDatiPubblicatiService datiPubblicatiService, ObbligoLeggeMapper obbligoLeggeMapper, CommonMapper commonMapper, IObbligoLeggeRepository obbligoLeggeRepository) {
        this.sezione23Repository = sezione23Repository;
        this.mongoUtils = mongoUtils;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.eventPublisher = eventPublisher;
        this.datiPubblicatiMapper = datiPubblicatiMapper;
        this.datiPubblicatiService = datiPubblicatiService;
        this.obbligoLeggeMapper = obbligoLeggeMapper;
        this.commonMapper = commonMapper;
        this.obbligoLeggeRepository = obbligoLeggeRepository;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public ObbligoLeggeDTO saveOrUpdate(ObbligoLeggeDTO obbligoLeggeDTO) {

        // Controllo base: il DTO non può essere nullo


        if (obbligoLeggeDTO == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        if (obbligoLeggeDTO.getIdSezione23() == null) {
            throw new IllegalArgumentException("IdSezione23 obbligatorio");
        }

        CycleAvoidingMappingContext context=new CycleAvoidingMappingContext();
        try {
            // Recupero lazy della sezione
            Sezione23 sezione = sezione23Repository.getReferenceById(obbligoLeggeDTO.getIdSezione23());

            ObbligoLegge entity;

            if (obbligoLeggeDTO.getId() != null) {
                // UPDATE
                entity = obbligoLeggeRepository.findById(obbligoLeggeDTO.getId())
                    .orElseThrow(() -> new RuntimeException("ObbligoLegge non trovato"));

                eventPublisher.publishEvent(new BeforeUpdateEvent<>(ObbligoLegge.class, entity));
            } else {
                // CREATE
                entity = new ObbligoLegge();
            }

            // Aggiorno campi base
            entity.setDenominazione(obbligoLeggeDTO.getDenominazione());
            entity.setDescrizione(obbligoLeggeDTO.getDescrizione());
            entity.setSezione23(sezione);



            // Salvataggio
            ObbligoLegge saved = obbligoLeggeRepository.save(entity);

            // Salvataggio DatiPubblicati tramite service Dati Pubblicati
            if (obbligoLeggeDTO.getDatiPubblicati() != null
                && !obbligoLeggeDTO.getDatiPubblicati().isEmpty()) {

                obbligoLeggeDTO.getDatiPubblicati()
                    .forEach(dp -> dp.setIdObbligoLegge(saved.getId()));

                datiPubblicatiService.saveAll(obbligoLeggeDTO.getDatiPubblicati());
            }

            //  DTO di risposta
            ObbligoLeggeDTO response = obbligoLeggeMapper.toDto(saved, new CycleAvoidingMappingContext());

            // Ripopolo dati Mongo
            populateDatiPubblicatiDTO(response, saved);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

            return response;

        } catch (Exception e) {
            log.error("Errore saveOrUpdate ObbligoLegge id={}: {}", obbligoLeggeDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate ObbligoLegge", e);
        }
    }


    @Transactional(readOnly = true)
    @Override
    public List<ObbligoLeggeDTO> getAllBySezione23(Long idSezione23) {

        // controllo sulla sezione se esiste
        if (idSezione23 == null) {
            throw new IllegalArgumentException("L'ID della Sezione23 non può essere nullo");
        }

        try {
            // getReferenceById restituisce un proxy
            Sezione23 sezione23 = sezione23Repository.getReferenceById(idSezione23);

            // ci salviamo tutti gli obblighi della sezione 23
            List<ObbligoLegge> entities =
                obbligoLeggeRepository.getObiettivoPrevenzioneBySezione23(sezione23);

            // prendiamo le nostre entities e le mappiamo in DTO
            List<ObbligoLeggeDTO> result = entities.stream()
                .map(entity -> {
                    // Mapping base JPA → DTO
                    ObbligoLeggeDTO dto =
                        obbligoLeggeMapper.toDto(entity, new CycleAvoidingMappingContext());

                    //mappato il dto con i campi JPA, lo popoliamo con i campi MONGO
                    populateDatiPubblicatiDTO(dto, entity);

                    return dto;
                })
                .toList();

            // Evento di successo (coerente con gli altri service)
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(result));

            return result;

        } catch (Exception e) {
            log.error("Errore getAllBySezione23 id={} : {}", idSezione23, e.getMessage(), e);

            eventPublisher.publishEvent(new TransactionFailureEvent<>(new ObbligoLeggeDTO(), e));

            throw new RuntimeException("Errore recupero Obbligo Legge per Sezione23", e);
        }
    }



    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteById(Long id) {
        // evito il NullPointerException
        if (id == null) {
            throw new IllegalArgumentException("L'ID non può essere nullo");
        }

        // passato l'id lo cerchiamo e salviamo la corrispondenza come entity
        // se non trova nulla lanciamo l'errore runtime
        try {
            ObbligoLegge entity = obbligoLeggeRepository.findById(id).orElseThrow(() -> new RuntimeException("Obbligo Legge non trovato con id: " + id));

            // Pubblico evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(ObbligoLegge.class, entity));

            // eliminiamo la corrispondenza trovata
            obbligoLeggeRepository.delete(entity);

            // Cancellazione dati associati in Mongo
            if (entity.getDatiPubblicati() != null) {
                entity.getDatiPubblicati().forEach(dp -> {
                    if (dp.getId() != null) {
                        ulterioriInfoRepository.deleteByExternalId(dp.getId());
                    }
                });
            }

            // Pubblico evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(entity));
            log.info("ObbligoLegge con id={} cancellato con successo", id);


            // se qualcosa fallisce durante il processo di eliminazione lo vediamo e c'è il rollback
        } catch (Exception e) {
            log.error("Errore delete ObbligoLegge id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new ObbligoLegge(), e));
            throw new RuntimeException("Errore eliminazione ObbligoLegge", e);
        }

    }


    //enrichWithRelations = metodo che trasforma una entity spoglia in un DTO completo,
    // mappando relazioni JPA + campi Mongo, così che chi chiama il servizio non deve andare a recuperare nulla separatamente.
    @Override
    public ObbligoLeggeDTO enrichWithRelations(ObbligoLegge entity) {
        // Controllo input iniziale
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Entity o ID non possono essere nulli");
        }

        try {
            // mapper base JPA to DTO
            ObbligoLeggeDTO dto = obbligoLeggeMapper.toDto(entity, new CycleAvoidingMappingContext());

            //  arricchisco le  relazioni (JPA + Mongo)
            populateDatiPubblicatiDTO(dto, entity);

            //  Logging
            log.debug("ObbligoLeggeDTO con id={} arricchito con relazioni", entity.getId());

            return dto;

        } catch (Exception e) {
            log.error("Errore durante l'arricchimento dell'ObbligoLeggeDTO con id={}: {}",
                entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante l'arricchimento dell'ObbligoLeggeDTO", e);
        }
    }

    @Override
    public void loadMongoDataForObbligo(ObbligoLeggeDTO obbligoDTO) {
        if (obbligoDTO == null || obbligoDTO.getId() == null) {
            log.warn("ObbligoLeggeDTO o ID è null, skip caricamento MongoDB");
            return;
        }

        try {
            // Carica i dati MongoDB per ogni DatoPubblicato
            if (obbligoDTO.getDatiPubblicati() != null && !obbligoDTO.getDatiPubblicati().isEmpty()) {
                obbligoDTO.getDatiPubblicati().forEach(datoDTO -> {
                    if (datoDTO.getId() != null) {
                        // Carica UlterioriInfo MongoDB per il DatoPubblicato
                        UlterioriInfoDTO ulterioriInfo = Optional.ofNullable(
                                ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                                    datoDTO.getId(),
                                    Sezione.SEZIONE_23_DATIPUBBLICATI
                                )
                            )
                            .map(u -> commonMapper.ulterioriInfoEntityToDto(u, new CycleAvoidingMappingContext()))
                            .orElse(null);
                        datoDTO.setUlterioriInfo(ulterioriInfo);
                    }
                });
            }

            log.debug("Dati MongoDB caricati per ObbligoLegge id={}", obbligoDTO.getId());
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per ObbligoLegge id={}: {}",
                obbligoDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB ObbligoLegge", e);
        }
    }


    // --- POPOLARE DTO (GET) ---
    // serve solo quando leggiamo dati dal db, quindi lavora solo sul DTO DA RESTITUIRE
    // Popolare significa riempire un DTO con dati aggiuntivi, oltre a quelli già presenti nell’entità principale.
    private void populateDatiPubblicatiDTO(ObbligoLeggeDTO dto, ObbligoLegge entity) {
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        if (entity.getDatiPubblicati() != null && !entity.getDatiPubblicati().isEmpty()) {

            List<DatiPubblicatiDTO> datiDTO = entity.getDatiPubblicati().stream()
                .map(d -> {

                    DatiPubblicatiDTO datiPubblicatiDTO = datiPubblicatiMapper.toDTO(d,context);

                    UlterioriInfo mongoEntity =
                        ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                            d.getId(),
                            Sezione.SEZIONE_23_DATIPUBBLICATI
                        );

                    datiPubblicatiDTO.setUlterioriInfo(
                        mongoEntity != null
                            ? commonMapper.ulterioriInfoEntityToDto(
                            mongoEntity,
                            new CycleAvoidingMappingContext()
                        )
                            : null
                    );

                    return datiPubblicatiDTO;
                })
                .toList();

            dto.setDatiPubblicati(datiDTO);
        }
    }


    // --- SYNC (POST/PUT) ---
    // lo scopo è modificare il db all'inseirmento di nuovi dati
    private void syncDatiPubblicati(ObbligoLegge parent, List<DatiPubblicatiDTO> dtoList) {

        List<DatiPubblicati> managedList = parent.getDatiPubblicati();

        if (managedList == null) {
            managedList = new ArrayList<>();
            parent.setDatiPubblicati(managedList);
        }

        if (dtoList == null) {
            return; // PATCH safe: non faccio nulla
        }

        for (DatiPubblicatiDTO dto : dtoList) {

            DatiPubblicati entity = null;

            //  Se ha ID → cerco nella lista
            if (dto.getId() != null) {

                for (DatiPubblicati dp : managedList) {
                    if (dto.getId().equals(dp.getId())) {
                        entity = dp;
                        break;
                    }
                }

                // Se ha ID ma non lo trovo → errore
                if (entity == null) {
                    throw new RuntimeException(
                        "DatiPubblicati con id " + dto.getId() + " non appartiene a questo ObbligoLegge"
                    );
                }
            }

            //  Se NON ha ID → è una creazione
            if (dto.getId() == null) {
                entity = new DatiPubblicati();
                entity.setObbligoLegge(parent);
                managedList.add(entity);
            }

            // Aggiorno i campi (solo se non null, così è PATCH safe)
            if (dto.getDenominazione() != null)
                entity.setDenominazione(dto.getDenominazione());

            if (dto.getTipologia() != null)
                entity.setTipologia(dto.getTipologia());

            if (dto.getResponsabile() != null)
                entity.setResponsabile(dto.getResponsabile());

            if (dto.getTerminiScadenza() != null)
                entity.setTerminiScadenza(dto.getTerminiScadenza());

            if (dto.getModalitaMonitoraggio() != null)
                entity.setModalitaMonitoraggio(dto.getModalitaMonitoraggio());

            if (dto.getMotivazioneImpossibilita() != null)
                entity.setMotivazioneImpossibilita(dto.getMotivazioneImpossibilita());
        }
    }





}
