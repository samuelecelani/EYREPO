package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.OVPStrategiaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IIndicatoreRepository;
import it.ey.piao.api.repository.OVPRepository;
import it.ey.piao.api.repository.IOVPStrategiaRepository;
import it.ey.piao.api.service.IIndicatoreService;
import it.ey.piao.api.service.IOVPStrategiaService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OVPStrategiaServiceImpl implements IOVPStrategiaService {

    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaServiceImpl.class);

    private final IOVPStrategiaRepository iovpStrategiaRepository;
    private final OVPRepository ovpRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final IIndicatoreRepository indicatoreRepository;
    private final OVPStrategiaMapper ovpStrategiaMapper;
    private final IIndicatoreService indicatoreService;

    public OVPStrategiaServiceImpl(IOVPStrategiaRepository iovpStrategiaRepository, OVPRepository ovpRepository, ApplicationEventPublisher eventPublisher, IIndicatoreRepository indicatoreRepository, OVPStrategiaMapper ovpStrategiaMapper, IIndicatoreService indicatoreService) {
        this.iovpStrategiaRepository = iovpStrategiaRepository;
        this.ovpRepository = ovpRepository;
        this.eventPublisher = eventPublisher;
        this.indicatoreRepository = indicatoreRepository;
        this.ovpStrategiaMapper = ovpStrategiaMapper;
        this.indicatoreService = indicatoreService;
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
    @Transactional(propagation = Propagation.REQUIRED, timeout = 10)
    public void delete(Long id) {
        if(id == null){
            throw new IllegalArgumentException("ID è obbligatorio");
        }
        try {
            log.debug("Eliminazione OVPStrategia con id: {}", id);
            var ovpStrategia = iovpStrategiaRepository.findById(id);
            if(ovpStrategia.isEmpty()){
                throw new IllegalArgumentException("OVPStrategia con id " + id + " non trovata");
            }
            iovpStrategiaRepository.deleteById(id);
            log.info("OVPStrategia con id {} eliminata con successo", id);
        } catch (DataAccessException dae) {
            log.error("Errore DB in delete (OVPStrategia): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante l'eliminazione dello OVPStrategia", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in delete (OVPStrategia): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante l'eliminazione dello OVPStrategia", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
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
                strategiaDTO.getIndicatori().forEach(indDTO -> {
                    if (indDTO.getIndicatore() != null && indDTO.getIndicatore().getId() != null) {
                        // Usa enrichWithRelations per caricare tutti i dati MongoDB dell'indicatore
                        Indicatore indicatoreEntity = indicatoreRepository.getReferenceById(indDTO.getIndicatore().getId());
                        IndicatoreDTO indicatoreCompleto = indicatoreService.enrichWithRelations(indicatoreEntity);
                        indDTO.setIndicatore(indicatoreCompleto);
                    }
                });
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
