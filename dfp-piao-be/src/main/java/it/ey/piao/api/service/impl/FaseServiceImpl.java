package it.ey.piao.api.service.impl;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.FaseMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IFaseRepository;
import it.ey.piao.api.repository.ISezione22Repository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.repository.mongo.IAttivitaRepository;
import it.ey.piao.api.repository.mongo.IAttoreRepository;
import it.ey.piao.api.service.IFaseService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final StoricoModificaHelper storicoModificaHelper;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private static final Logger log = LoggerFactory.getLogger(FaseServiceImpl.class);


    public FaseServiceImpl(IFaseRepository faseRepository, FaseMapper faseMapper, MongoUtils mongoUtils, IAttivitaRepository attivitaRepository, IAttoreRepository attoreRepository, ISezione22Repository sezione22Repository, ApplicationEventPublisher eventPublisher, StoricoModificaHelper storicoModificaHelper, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.faseRepository = faseRepository;
        this.faseMapper = faseMapper;
        this.mongoUtils = mongoUtils;
        this.attivitaRepository = attivitaRepository;
        this.attoreRepository = attoreRepository;
        this.sezione22Repository = sezione22Repository;
        this.eventPublisher = eventPublisher;
        this.storicoModificaHelper = storicoModificaHelper;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
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

            Fase savedEntity = faseRepository.save(entity);
            response = faseMapper.toDto(savedEntity,context);

            response.setAttivita(
                Optional.ofNullable(fase.getAttivita())
                    .map(dto -> {
                        Attivita entityMongo = faseMapper.attivitaToEntity(dto,context);
                        entityMongo.setExternalId(entity.getId());
                        return entityMongo;
                    })
                    .map(e -> mongoUtils.saveItem(e, entity.getId(), attivitaRepository, Attivita.class))
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
                    .map(e -> mongoUtils.saveItem(e, entity.getId(), attoreRepository, Attore.class,
                        en -> en.setTipoSezione(Sezione.PIAO),
                        "tipoSezione", Sezione.PIAO))
                    .map(saved -> faseMapper.attoreToDto(saved,context))
                    .orElse(null)
            );

            if (fase.getCampiModificati() != null && !fase.getCampiModificati().isBlank() && fase.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(fase, response.getIdSezione22(), fase.getIdPiao(), Sezione.SEZIONE_22);
            }
            if (fase.getStatoSezione() != null && !fase.getStatoSezione().isBlank() &&  !StatoEnum.fromDescrizione(fase.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getSezione22().getId(),Sezione.SEZIONE_22.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(fase.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(fase.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getSezione22().getId())
                        .codTipologiaFK(Sezione.SEZIONE_22.name())
                        .testo(StatoEnum.fromDescrizione(fase.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(fase.getUpdatedByNameSurname())
                        .createdByRole(fase.getUpdatedByRole())
                        .build());
            }
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));
        } catch (Exception e) {
            log.error("Errore durante Save o update  per fase id={}: {}", fase.getId(), e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(faseMapper.toEntity(fase,context), e));
            throw new RuntimeException("Errore durante il save o update della Fase", e);
        }
    }

    @Override
    public void deleteFase(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
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
            faseRepository.softDeleteById(id, LocalDateTime.now());

            // Propagazione della cancellazione su MongoDB
            attivitaRepository.deleteByExternalId(id);
            attoreRepository.deleteByExternalId(id);

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
                storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getSezione22().getId(), idPiao, Sezione.SEZIONE_22);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getSezione22().getId(), Sezione.SEZIONE_22.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getSezione22().getId())
                            .codTipologiaFK(Sezione.SEZIONE_22.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

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
