package it.ey.piao.api.service.impl;

import it.ey.dto.AttivitaDTO;
import it.ey.dto.AttoreDTO;
import it.ey.dto.FaseDTO;
import it.ey.dto.SocialDTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.piao.api.mapper.FaseMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IFaseRepository;
import it.ey.piao.api.repository.ISezione22Repository;
import it.ey.piao.api.repository.mongo.IAttivitaRepository;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.service.IFaseService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class FaseServiceImpl implements IFaseService {


    private final IFaseRepository faseRepository;
    private final FaseMapper faseMapper;
    private final MongoUtils mongoUtils;
    private final IAttivitaRepository attivitaRepository;
    private final IAttoreRepository attoreRepository;
    private final ISezione22Repository sezione22Repository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger log = LoggerFactory.getLogger(FaseServiceImpl.class);


    public FaseServiceImpl(IFaseRepository faseRepository, FaseMapper faseMapper, MongoUtils mongoUtils, IAttivitaRepository attivitaRepository, IAttoreRepository attoreRepository, ISezione22Repository sezione22Repository, ApplicationEventPublisher eventPublisher) {
        this.faseRepository = faseRepository;
        this.faseMapper = faseMapper;
        this.mongoUtils = mongoUtils;
        this.attivitaRepository = attivitaRepository;
        this.attoreRepository = attoreRepository;
        this.sezione22Repository = sezione22Repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void saveOrUpdateFase(FaseDTO fase) {
        FaseDTO response = null;
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            Fase entity = faseMapper.toEntity(fase,context);
            entity.setSezione22(sezione22Repository.getReferenceById(fase.getIdSezione22()));
            //Salvo lo stato dell'oggetto per un eventuale rollback
            if (entity.getId() != null) {
                faseRepository.findById(entity.getId()).ifPresent(existing -> {
                eventPublisher.publishEvent(new BeforeUpdateEvent<>(Fase.class, existing));
            });}


            response = faseMapper.toDto(faseRepository.save(entity),context);

            response.setAttivita(
                Optional.ofNullable(fase.getAttivita())
                    .map(dto -> {
                        Attivita entityMongo = faseMapper.attivitaToEntity(dto,context);
                        entityMongo.setExternalId(entity.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtils.saveAllItems(e, attivitaRepository, Attivita.class))
                    .map(saved -> faseMapper.attivitaToDto(saved,context))
                    .orElse(null)
            );

            response.setAttore(
                Optional.ofNullable(fase.getAttore())
                    .map(dto -> {
                        Attore entityMongo = faseMapper.attoreToEntity(dto,context);
                        entityMongo.setExternalId(entity.getId());
                        entityMongo.setIdPiao(entity.getSezione22().getPiao().getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtils.saveItem(e, attoreRepository, Attore.class,
                        en -> en.setTipoSezione(Sezione.PIAO)))
                    .map(saved -> faseMapper.attoreToDto(saved,context))
                    .orElse(null)
            );

            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
        } catch (Exception e) {
            log.error("Errore durante Save o update  per fase id={}: {}", fase.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(faseMapper.toEntity(fase,context), e));
            throw new RuntimeException("Errore durante il save o update della Fase", e);
        }
    }

    @Override
    public void deleteFase(Long id) {
        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<Fase> existing = faseRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare una fase non esistente con id={}", id);
                throw new RuntimeException("Fase non trovata con id: " + id);
            }

            // Pubblico evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(Fase.class, existing.get()));

            // Cancellazione da PostgreSQL
            faseRepository.deleteById(id);

            // Propagazione della cancellazione su MongoDB
            attivitaRepository.deleteByExternalId(id);
            attoreRepository.deleteByExternalId(id);

            // Pubblico evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("Fase con id={} cancellata con successo", id);
        } catch (Exception e) {
            log.error("Errore durante la cancellazione della fase id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new Fase(), e));
            throw new RuntimeException("Errore durante la cancellazione della Fase", e);
        }
    }

    @Override
    public void saveOrUpdateAll(java.util.List<FaseDTO> fasi) {
        if (fasi == null || fasi.isEmpty()) {
            log.debug("Lista fasi vuota o null, skip salvataggio batch");
            return;
        }

        log.info("Salvataggio batch di {} fasi", fasi.size());

        try {
            for (FaseDTO fase : fasi) {
                saveOrUpdateFase(fase);
            }
            log.info("Batch salvataggio completato: {} fasi salvate", fasi.size());
        } catch (Exception e) {
            log.error("Errore durante il salvataggio batch delle fasi: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch delle fasi", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void loadMongoDataForFase(FaseDTO faseDTO) {
        if (faseDTO == null || faseDTO.getId() == null) {
            log.warn("FaseDTO o ID è null, skip caricamento MongoDB");
            return;
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();


        try {
            // Carica Attore da MongoDB
            faseDTO.setAttore(
                Optional.ofNullable(attoreRepository.findAllByExternalIdAndTipoSezione(faseDTO.getId(),Sezione.PIAO))
                    .map(u -> faseMapper.attoreToDto(u,context))
                    .orElse(null)
            );

            // Carica Attività da MongoDB
            faseDTO.setAttivita(
                Optional.ofNullable(attivitaRepository.getByExternalId(faseDTO.getId()))
                    .map(u -> faseMapper.attivitaToDto(u,context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB caricati per Fase id={}", faseDTO.getId());
        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per Fase id={}: {}", faseDTO.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB Fase", e);
        }
    }
}
