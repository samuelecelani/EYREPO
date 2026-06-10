package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.DatiPubblicatiDTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.CommonMapper;
import it.ey.piao.api.mapper.DatiPubblicatiMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IDatiPubblicatiRepository;
import it.ey.piao.api.repository.IObbligoLeggeRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.mongo.IUlterioriInfoRepository;
import it.ey.piao.api.service.IDatiPubblicatiService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DatiPubblicatiServiceImpl implements IDatiPubblicatiService {
    private static final Logger log = LoggerFactory.getLogger(DatiPubblicatiServiceImpl.class);

    private final IDatiPubblicatiRepository datiPubblicatiRepository;
    private final IObbligoLeggeRepository obbligoLeggeRepository;
    private final IUlterioriInfoRepository ulterioriInfoRepository;
    private final DatiPubblicatiMapper datiPubblicatiMapper;
    private final CommonMapper commonMapper;
    private final MongoUtils mongoUtils;
    private final ApplicationEventPublisher eventPublisher;
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public DatiPubblicatiServiceImpl(
        IDatiPubblicatiRepository datiPubblicatiRepository,
        IObbligoLeggeRepository obbligoLeggeRepository,
        IUlterioriInfoRepository ulterioriInfoRepository,
        DatiPubblicatiMapper datiPubblicatiMapper,
        CommonMapper commonMapper,
        MongoUtils mongoUtils,
        ApplicationEventPublisher eventPublisher,
        StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository
    ) {
        this.datiPubblicatiRepository = datiPubblicatiRepository;
        this.obbligoLeggeRepository = obbligoLeggeRepository;
        this.ulterioriInfoRepository = ulterioriInfoRepository;
        this.datiPubblicatiMapper = datiPubblicatiMapper;
        this.commonMapper = commonMapper;
        this.mongoUtils = mongoUtils;
        this.eventPublisher = eventPublisher;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public DatiPubblicatiDTO saveOrUpdate(DatiPubblicatiDTO dto) {

        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere nulla");
        }

        DatiPubblicatiDTO response;

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {

            // DTO → Entity JPA
            DatiPubblicati entity = datiPubblicatiMapper.toEntity(dto,context);

            // Relazione con ObbligoLegge
            entity.setObbligoLegge(
                obbligoLeggeRepository.getReferenceById(dto.getIdObbligoLegge())
            );

            // Se update pubblico evento BeforeUpdate
            if (entity.getId() != null) {
                datiPubblicatiRepository.findById(entity.getId())
                    .ifPresent(existing ->
                        eventPublisher.publishEvent(
                            new BeforeUpdateEvent<>(DatiPubblicati.class, existing)
                        )
                    );
            }

            // Salvataggio su DB relazionale
            DatiPubblicati savedEntity = datiPubblicatiRepository.save(entity);

            // Entity → DTO risposta
            response = datiPubblicatiMapper.toDTO(savedEntity,context);

            // ==========================
            // Gestione Mongo - UlterioriInfo
            // ==========================

            if (dto.getUlterioriInfo() != null) {

                UlterioriInfo entityMongo =
                    commonMapper.ulterioriInfoDtoToEntity(
                        dto.getUlterioriInfo(),
                        new CycleAvoidingMappingContext()
                    );

                entityMongo.setExternalId(savedEntity.getId());
                entityMongo.setTipoSezione(Sezione.SEZIONE_23_DATIPUBBLICATI);

                // Cerca se esiste già
                UlterioriInfo existing = ulterioriInfoRepository
                    .findByExternalIdAndTipoSezione(entityMongo.getExternalId(), entityMongo.getTipoSezione());

                UlterioriInfo savedInfo;
                if (existing != null) {
                    // Aggiorna documento esistente
                    existing.setProperties(entityMongo.getProperties());
                    savedInfo = ulterioriInfoRepository.save(existing);
                } else {
                    // Salva nuovo documento
                    savedInfo = ulterioriInfoRepository.save(entityMongo);
                }

                response.setUlterioriInfo(
                    commonMapper.ulterioriInfoEntityToDto(savedInfo, new CycleAvoidingMappingContext())
                );
            }

            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(dto, savedEntity.getObbligoLegge().getSezione23().getId(), dto.getIdPiao(), Sezione.SEZIONE_23);
            }
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() && StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getObbligoLegge().getSezione23().getId(),Sezione.SEZIONE_23.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getObbligoLegge().getSezione23().getId())
                        .codTipologiaFK(Sezione.SEZIONE_23.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }
        } catch (Exception e) {

            log.error(
                "Errore durante Save o update DatiPubblicati id={}: {}",
                dto.getId(),
                e.getMessage(),
                e
            );

            eventPublisher.publishEvent(
                new TransactionFailureEvent<>(
                    datiPubblicatiMapper.toEntity(dto,context),
                    e
                )
            );

            throw new RuntimeException(
                "Errore durante il save o update dei Dati Pubblicati",
                e
            );
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatiPubblicatiDTO> getAllByObbligoLeggeId(Long idObbligoLegge) {
        if (idObbligoLegge == null) {
            throw new IllegalArgumentException("L'ID dell'ObbligoLegge non può essere nullo");
        }

        List<DatiPubblicatiDTO> response;

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
        try {

            // Recupero riferimento ObbligoLegge
            ObbligoLegge obbligoLegge =
                obbligoLeggeRepository.getReferenceById(idObbligoLegge);

            // Recupero tutti i DatiPubblicati associati
            List<DatiPubblicati> entities =
                datiPubblicatiRepository.findByIdObbligoLegge(obbligoLegge.getId());

            // Mapping Entity → DTO con eventuale arricchimento Mongo
            response = entities.stream()
                .map(entity -> {

                    // Mapping base JPA → DTO
                    DatiPubblicatiDTO dto =
                        datiPubblicatiMapper.toDTO(entity,context);

                    // Recupero UlterioriInfo da Mongo
                    dto.setUlterioriInfo(
                        Optional.ofNullable(
                                ulterioriInfoRepository.findByExternalIdAndTipoSezione(
                                    entity.getId(),
                                    Sezione.SEZIONE_23_DATIPUBBLICATI
                                )
                            )
                            .map(info ->
                                commonMapper.ulterioriInfoEntityToDto(info, new CycleAvoidingMappingContext())).orElse(null)
                    );

                    return dto;
                })
                .toList();

            // Evento di successo
            eventPublisher.publishEvent(
                new TransactionSuccessEvent<>(response)
            );

        } catch (Exception e) {
            log.error(
                "Errore durante il recupero dei DatiPubblicati per ObbligoLegge id={}: {}", idObbligoLegge, e.getMessage(), e
            );
            eventPublisher.publishEvent(new TransactionFailureEvent<>(DatiPubblicati.class, e)
            );

            throw new RuntimeException("Errore durante il recupero dei Dati Pubblicati", e);
        }
        return response;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID di DatiPubblicati non può essere nullo");
        }

        try {
            // Recupero entità prima della cancellazione
            Optional<DatiPubblicati> existing = datiPubblicatiRepository.findById(id);

            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare DatiPubblicati non esistente con id={}", id);
                throw new RuntimeException("DatiPubblicati non trovato con id: " + id);
            }

            // Evento prima della cancellazione
            eventPublisher.publishEvent(
                new BeforeUpdateEvent<>(DatiPubblicati.class, existing.get())
            );

            // Cancellazione da Postgres
            datiPubblicatiRepository.softDeleteById(id, LocalDateTime.now());

            // Cancellazione eventuale UlterioriInfo da Mongo
            ulterioriInfoRepository.deleteByExternalId(id);

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
                storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getObbligoLegge().getSezione23().getId(), idPiao, Sezione.SEZIONE_23);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getObbligoLegge().getSezione23().getId(), Sezione.SEZIONE_23.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getObbligoLegge().getSezione23().getId())
                            .codTipologiaFK(Sezione.SEZIONE_23.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            // Evento di successo
            eventPublisher.publishEvent(
                new TransactionSuccessEvent<>(existing.get()));

            log.info("DatiPubblicati con id={} cancellato con successo", id);

        } catch (Exception e) {

            log.error("Errore durante la cancellazione di DatiPubblicati id={}: {}", id, e.getMessage(), e);

            eventPublisher.publishEvent(new TransactionFailureEvent<>(new DatiPubblicati(), e));

            throw new RuntimeException("Errore durante la cancellazione di DatiPubblicati", e);}
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 60)
    public void saveAll(List<DatiPubblicatiDTO> requests) {

        if (requests == null || requests.isEmpty()) {
            log.debug("Lista DatiPubblicati vuota o null, skip salvataggio batch");
            return;
        }

        log.info("Salvataggio batch di {} DatiPubblicati", requests.size());

        try {

            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
            List<DatiPubblicati> entitiesToSave = new ArrayList<>();

            for (DatiPubblicatiDTO dto : requests) {

                if (dto == null) {
                    log.warn("DatiPubblicatiDTO nullo nella lista, skip");
                    continue;
                }

                // Mappo DTO -> Entity
                DatiPubblicati entity = datiPubblicatiMapper.toEntity(dto, context);

                // Relazione con ObbligoLegge
                entity.setObbligoLegge(
                    obbligoLeggeRepository.getReferenceById(dto.getIdObbligoLegge())
                );

                entitiesToSave.add(entity);
            }

            // Salvataggio batch con saveAll di JPA
            List<DatiPubblicati> savedEntities = datiPubblicatiRepository.saveAll(entitiesToSave);

            log.info("Batch salvataggio completato: {} DatiPubblicati salvati", savedEntities.size());

            // Salvataggio Mongo per ogni DTO originale
            for (DatiPubblicatiDTO dto : requests) {

                if (dto != null && dto.getUlterioriInfo() != null) {

                    // Recupero l'ID salvato cercando quello appena persistito
                    DatiPubblicati savedEntity = savedEntities.stream()
                        .filter(e ->
                            dto.getId() != null
                                ? e.getId().equals(dto.getId())
                                : true
                        )
                        .findFirst()
                        .orElse(null);

                    if (savedEntity != null) {

                        UlterioriInfo entityMongo =
                            commonMapper.ulterioriInfoDtoToEntity(dto.getUlterioriInfo(), new CycleAvoidingMappingContext());

                        entityMongo.setExternalId(savedEntity.getId());

                        mongoUtils.saveItem(
                            entityMongo,
                            savedEntity.getId(),
                            ulterioriInfoRepository,
                            UlterioriInfo.class,
                            en -> en.setTipoSezione(Sezione.SEZIONE_23_DATIPUBBLICATI),
                            "tipoSezione", Sezione.SEZIONE_23_DATIPUBBLICATI
                        );
                    }
                }
            }

        } catch (Exception e) {

            log.error("Errore durante il salvataggio batch dei DatiPubblicati: {}",
                e.getMessage(), e);

            throw new RuntimeException(
                "Errore durante il salvataggio batch dei DatiPubblicati", e);
        }
    }



}
