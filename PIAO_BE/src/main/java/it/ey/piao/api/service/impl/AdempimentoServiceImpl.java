package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.AdempimentoMapper;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAdempimentoRepository;
import it.ey.piao.api.repository.ISezione22Repository;
import it.ey.piao.api.repository.mongo.IAzioneRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IAdempimentoService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class AdempimentoServiceImpl implements IAdempimentoService
{
    private final AdempimentoMapper adempimentoMapper;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;
    private final IAdempimentoRepository adempimentoRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final IAzioneRepository azioneRepository;
    private final ISezione22Repository sezione22Repository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger log = LoggerFactory.getLogger(AdempimentoServiceImpl.class);

    public AdempimentoServiceImpl(AdempimentoMapper adempimentoMapper, CommonMapper commonMapper, MongoUtils mongoUtils, IAdempimentoRepository adempimentoRepository, IUlterioriInfoRepository ulterioriInfoRepository, IAzioneRepository azioneRepository, ISezione22Repository sezione22Repository, ApplicationEventPublisher applicationEventPublisher) {
        this.adempimentoMapper = adempimentoMapper;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
        this.adempimentoRepository = adempimentoRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.azioneRepository = azioneRepository;
        this.sezione22Repository = sezione22Repository;
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void saveOrUpdate(AdempimentoDTO adempimentoDTO)
    {
        AdempimentoDTO response;
        try{
            Adempimento entity = adempimentoMapper.toEntity(adempimentoDTO);
            entity.setSezione22(sezione22Repository.getReferenceById(adempimentoDTO.getIdSezione22()));
            //Salvo lo stato dell'oggetto per un eventuale rollback
            if (entity.getId() != null)
            {
                adempimentoRepository.findById(entity.getId()).ifPresent(existing ->
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(Adempimento.class, existing)));
            }

            Adempimento saveEntity = adempimentoRepository.save(entity);
            response = adempimentoMapper.toDto(saveEntity);

            // Salva i dati MongoDB tramite metodo dedicato
            saveMongoDataAdempimento(response, adempimentoDTO);

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
        }catch (Exception e)
        {
            log.error("Errore durante Save o update  per fase id={}: {}", adempimentoDTO.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(adempimentoMapper.toEntity(adempimentoDTO), e));
            throw new RuntimeException("Errore durante il save o update dell'Adempimento", e);
        }
    }

    @Override
    public AdempimentoDTO loadMongoDataAdempimento(AdempimentoDTO adempimento) {
        if (adempimento == null || adempimento.getId() == null) {
            log.warn("AdempimentoDTO o id è null, skip caricamento MongoDB");
            return adempimento;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Caricamento UlterioriInfo da MongoDB
            adempimento.setUlterioriInfo(
                Optional.ofNullable(ulterioriInfoRepository.findByExternalIdAndTipoSezione(adempimento.getId(), Sezione.SEZIONE_22_ADEMPIMENTO))
                    .map(ultInfo -> commonMapper.ulterioriInfoEntityToDto(ultInfo, context))
                    .orElse(null)
            );

            // Caricamento Azione da MongoDB
            adempimento.setAzione(
                Optional.ofNullable(azioneRepository.getByExternalId(adempimento.getId()))
                    .map(azione -> commonMapper.azioneEntityToDto(azione, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB caricati per Adempimento id={}", adempimento.getId());
            return adempimento;
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per Adempimento id={}: {}", adempimento.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB Adempimento", e);
        }
    }

    @Override
    public void saveMongoDataAdempimento(AdempimentoDTO response, AdempimentoDTO request) {
        if (response == null || response.getId() == null || request == null) {
            log.warn("Response, Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva UlterioriInfo MongoDB
            response.setUlterioriInfo(
                Optional.ofNullable(request.getUlterioriInfo())
                    .map(addInfoDTO -> {
                        UlterioriInfo entityMongo = commonMapper.ulterioriInfoDtoToEntity(addInfoDTO, context);
                        entityMongo.setExternalId(response.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtils.saveItem(
                        e,
                        ulterioriInfoRepository,
                        UlterioriInfo.class,
                        en -> en.setTipoSezione(Sezione.SEZIONE_22_ADEMPIMENTO)
                    ))
                    .map(savedAddInfo -> commonMapper.ulterioriInfoEntityToDto(savedAddInfo, context))
                    .orElse(null)
            );

            // Salva Azione MongoDB
            response.setAzione(
                Optional.ofNullable(request.getAzione())
                    .map(dto -> {
                        Azione entityMongo = commonMapper.azioneDtoToEntity(dto, context);
                        entityMongo.setExternalId(response.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtils.saveAllItems(e, azioneRepository, Azione.class))
                    .map(saved -> commonMapper.azioneEntityToDto(saved, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB salvati per Adempimento id={}", response.getId());
        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per Adempimento id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB Adempimento", e);
        }
    }

    @Override
    public void deleteAdempimento(Long id)
    {
        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<Adempimento> existing = adempimentoRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un adempimento non esistente con id={}", id);
                throw new RuntimeException("Adempimento non trovato con id: " + id);
            }

            // Pubblico evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(Adempimento.class, existing.get()));

            // Cancellazione da PostgreSQL
            adempimentoRepository.deleteById(id);

            // Propagazione della cancellazione su MongoDB
            azioneRepository.deleteByExternalId(id);
            ulterioriInfoRepository.deleteByExternalId(id);

            // Pubblico evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("Adempimento con id={} cancellato con successo", id);
        } catch (Exception e) {
            log.error("Errore durante la cancellazione dell'adempimento id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new Adempimento(), e));
            throw new RuntimeException("Errore durante la cancellazione dell'Adempimento", e);
        }
    }

    @Override
    public void saveOrUpdateAll(java.util.List<AdempimentoDTO> adempimenti) {
        if (adempimenti == null || adempimenti.isEmpty()) {
            log.debug("Lista adempimenti vuota o null, skip salvataggio batch");
            return;
        }

        log.info("Salvataggio batch di {} adempimenti", adempimenti.size());

        try {
            for (AdempimentoDTO adempimento : adempimenti) {
                saveOrUpdate(adempimento);
            }
            log.info("Batch salvataggio completato: {} adempimenti salvati", adempimenti.size());
        } catch (Exception e) {
            log.error("Errore durante il salvataggio batch degli adempimenti: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch degli adempimenti", e);
        }
    }
}
