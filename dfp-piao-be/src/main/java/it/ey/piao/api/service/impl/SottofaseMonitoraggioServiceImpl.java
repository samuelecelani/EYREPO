package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.SottofaseMonitoraggioDTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.SottofaseMonitoraggioMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.service.ISottofaseMonitoraggioService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
import it.ey.piao.api.utilsPrivate.MongoUtils;
import it.ey.utils.StoricoStatoSezioneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SottofaseMonitoraggioServiceImpl implements ISottofaseMonitoraggioService {
    private final ISottofaseMonitoraggioRepository sottofaseMonitoraggioRepository;
    private final SottofaseMonitoraggioMapper sottofaseMonitoraggioMapper;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;
    private final IAttoreRepository attoreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ISezione4Repository sezione4Repository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;
    private final ICategoriaObiettiviRepository categoriaObiettiviRepository;
    private final IMilestoneRepository milestoneRepository;
    private final IPromemoriaRepository promemoriaRepository;

    private static final Logger log = LoggerFactory.getLogger(SottofaseMonitoraggioServiceImpl.class);

    public SottofaseMonitoraggioServiceImpl(ISottofaseMonitoraggioRepository sottofaseMonitoraggioRepository,
                                            SottofaseMonitoraggioMapper sottofaseMonitoraggioMapper,
                                            CommonMapper commonMapper,
                                            MongoUtils mongoUtils,
                                            IAttoreRepository attoreRepository,
                                            ApplicationEventPublisher eventPublisher,
                                            ISezione4Repository sezione4Repository,
                                            StoricoModificaHelper storicoModificaHelper,
                                            IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                            ICategoriaObiettiviRepository categoriaObiettiviRepository,
                                            IMilestoneRepository milestoneRepository,
                                            IPromemoriaRepository promemoriaRepository) {
        this.sottofaseMonitoraggioRepository = sottofaseMonitoraggioRepository;
        this.sottofaseMonitoraggioMapper = sottofaseMonitoraggioMapper;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
        this.attoreRepository = attoreRepository;
        this.eventPublisher = eventPublisher;
        this.sezione4Repository = sezione4Repository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.categoriaObiettiviRepository = categoriaObiettiviRepository;
        this.milestoneRepository = milestoneRepository;
        this.promemoriaRepository = promemoriaRepository;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public SottofaseMonitoraggioDTO saveOrUpdate(SottofaseMonitoraggioDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        log.debug("DTO ricevuto nel service: {}", dto);
        log.debug("idSezione4 = {}", dto.getIdSezione4());

        SottofaseMonitoraggioDTO response;
        try {
            // DTO → Entity JPA
            SottofaseMonitoraggio entity = sottofaseMonitoraggioMapper.toEntity(dto, context);

            // Relazione con Sezione4
            if (dto.getIdSezione4() == null) {
                throw new IllegalArgumentException("idSezione4 non può essere null");
            }
            Sezione4 s4 = sezione4Repository.getReferenceById(dto.getIdSezione4());
            entity.setSezione4(s4);

            // Pulizia Promemoria vuoti creati dal mapper quando idPromemoria è null
            if (entity.getMilestone() != null) {
                for (Milestone m : entity.getMilestone()) {
                    if (m.getPromemoria() != null)
                    {
                        Long promemoriaID = m.getPromemoria().getId();
                        if(promemoriaID == null)
                        {
                            m.setPromemoria(null);
                        } else {
                            m.setPromemoria(promemoriaRepository.getReferenceById(promemoriaID));
                        }
                    }
                }
            }

            // Salvo lo stato dell'entity per eventuale rollback
            if (entity.getId() != null) {
                sottofaseMonitoraggioRepository.findById(entity.getId())
                    .ifPresent(existing ->
                        eventPublisher.publishEvent(new BeforeUpdateEvent<>(SottofaseMonitoraggio.class, existing))
                    );
            }

            // Salvo l'entity principale nel DB relazionale
            SottofaseMonitoraggio savedEntity = sottofaseMonitoraggioRepository.save(entity);

            // Mappo l'entity salvata in DTO di risposta
            response = sottofaseMonitoraggioMapper.toDto(savedEntity, context);

            // Salva i dati MongoDB (attore)
            saveMongoData(response, dto);

            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(dto, dto.getIdSezione4(), dto.getIdPiao(), Sezione.SEZIONE_4);
            }
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getSezione4().getId(),Sezione.SEZIONE_4.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getSezione4().getId())
                        .codTipologiaFK(Sezione.SEZIONE_4.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }
            // Pubblico evento successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante Save o update per SottofaseMonitoraggio id={}: {}", dto.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(sottofaseMonitoraggioMapper.toEntity(dto, context), e));
            throw new RuntimeException("Errore durante il save o update della SottofaseMonitoraggio", e);
        }

        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public List<SottofaseMonitoraggioDTO> getAllBySezione4(Long idSezione4) {
        if (idSezione4 == null) {
            throw new IllegalArgumentException("L'ID della Sezione4 non può essere nullo");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        List<SottofaseMonitoraggioDTO> response;
        try {
            // Recupero il riferimento alla Sezione4 senza eseguire subito la query
            Sezione4 sezione4 = sezione4Repository.getReferenceById(idSezione4);

            // Recupero tutte le SottofaseMonitoraggio associate alla Sezione4
            List<SottofaseMonitoraggio> entities = sottofaseMonitoraggioRepository.getSottofaseMonitoraggioBySezione4(sezione4);

            // Mapping Entity → DTO con arricchimento dei dati Mongo (attore)
            response = entities.stream()
                .map(entity -> {
                    // Mapping base JPA → DTO
                    SottofaseMonitoraggioDTO dto = sottofaseMonitoraggioMapper.toDto(entity, context);

                    // Carica dati MongoDB
                    dto = loadMongoData(dto);

                    return dto;
                })
                .toList();

            // Pubblico evento di successo della transazione
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante il recupero delle SottofaseMonitoraggio per Sezione4 id={}: {}",
                idSezione4, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(SottofaseMonitoraggio.class, e));
            throw new RuntimeException("Errore durante il recupero delle SottofaseMonitoraggio per Sezione4", e);
        }

        return response;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID della SottofaseMonitoraggio non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<SottofaseMonitoraggio> existing = sottofaseMonitoraggioRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare una SottofaseMonitoraggio non esistente con id={}", id);
                throw new RuntimeException("SottofaseMonitoraggio non trovata con id: " + id);
            }

            // Evento prima della cancellazione
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(SottofaseMonitoraggio.class, existing.get()));

            // Cancellazione da Postgres
            LocalDateTime deactivationTime = LocalDateTime.now();
            // SOFT DELETE relazione con categoria
            categoriaObiettiviRepository.softDeleteBySottofaseId(id,deactivationTime);

            // SOFT DELETE relazione con MILESTONE
            milestoneRepository.softDeleteBySottofaseMonitoraggioId(id,deactivationTime);

            sottofaseMonitoraggioRepository.softDeleteById(id,deactivationTime);
            // Propagazione della cancellazione su MongoDB (attore)
            attoreRepository.deleteByExternalIdAndTipoSezione(id, Sezione.SEZIONE_4_SOTTOFASEMONITORAGGIO);

            // Salva storico modifica dopo la cancellazione
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null) {
                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();
                storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getSezione4().getId(), idPiao, Sezione.SEZIONE_4);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getSezione4().getId(), Sezione.SEZIONE_4.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getSezione4().getId())
                            .codTipologiaFK(Sezione.SEZIONE_4.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            // Evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("SottofaseMonitoraggio con id={} cancellata con successo", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione della SottofaseMonitoraggio id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new SottofaseMonitoraggio(), e));
            throw new RuntimeException("Errore durante la cancellazione della SottofaseMonitoraggio", e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public SottofaseMonitoraggioDTO loadMongoData(SottofaseMonitoraggioDTO sottofase) {
        if (sottofase == null || sottofase.getId() == null) {
            log.warn("SottofaseMonitoraggioDTO o id è null, skip caricamento MongoDB");
            return sottofase;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Recupero Attore da MongoDB
            sottofase.setAttore(
                Optional.ofNullable(
                        attoreRepository.findAllByExternalIdAndTipoSezione(
                            sottofase.getId(),
                            Sezione.SEZIONE_4_SOTTOFASEMONITORAGGIO
                        )
                    )
                    .map(attore -> commonMapper.attoreEntityToDto(attore, context))
                    .orElse(null)
            );

            log.debug("Dati MongoDB caricati per SottofaseMonitoraggio id={}", sottofase.getId());
            return sottofase;

        } catch (Exception e) {
            log.error("Errore caricamento dati MongoDB per SottofaseMonitoraggio id={}: {}", sottofase.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore caricamento dati MongoDB SottofaseMonitoraggio", e);
        }
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveMongoData(SottofaseMonitoraggioDTO response, SottofaseMonitoraggioDTO request) {
        if (response == null || response.getId() == null || request == null) {
            log.warn("Response, Request o ID è null, skip salvataggio MongoDB");
            return;
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Salva Attore in MongoDB
            Attore attoreEntity = request.getAttore() != null
                ? commonMapper.attoreDtoToEntity(request.getAttore(), context)
                : null;
            if (attoreEntity != null) {
                attoreEntity.setExternalId(response.getId());
                attoreEntity.setTipoSezione(Sezione.SEZIONE_4_SOTTOFASEMONITORAGGIO);
            }
            Attore savedAttore = mongoUtils.saveItem(attoreEntity, response.getId(),
                attoreRepository, Attore.class,
                en -> en.setTipoSezione(Sezione.SEZIONE_4_SOTTOFASEMONITORAGGIO),
                "tipoSezione", Sezione.SEZIONE_4_SOTTOFASEMONITORAGGIO);
            response.setAttore(savedAttore != null
                ? commonMapper.attoreEntityToDto(savedAttore, context)
                : null);

            log.debug("Dati MongoDB salvati per SottofaseMonitoraggio id={}", response.getId());

        } catch (Exception e) {
            log.error("Errore salvataggio dati MongoDB per SottofaseMonitoraggio id={}: {}", response.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore salvataggio dati MongoDB SottofaseMonitoraggio", e);
        }
    }

}
