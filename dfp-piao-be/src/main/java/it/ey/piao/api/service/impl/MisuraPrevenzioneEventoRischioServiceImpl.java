package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioIndicatoreDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioStakeholderDTO;
import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.IMisuraPrevenzioneEventoRischioMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.*;
import it.ey.piao.api.service.IMisuraPrevenzioneEventoRischioService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import it.ey.piao.api.service.helper.StoricoModificaHelper;
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
public class MisuraPrevenzioneEventoRischioServiceImpl implements IMisuraPrevenzioneEventoRischioService {

    private final IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository;
    private static final Logger log = LoggerFactory.getLogger(MisuraPrevenzioneEventoRischioServiceImpl.class);
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
    private final IEventoRischioRepository eventoRischioRepository;
    private final IMisuraPrevenzioneEventoRischioMapper prevenzioneEventoRischioMapper;
    private final IIndicatoreRepository indicatoreRepository;
    private final IStakeHolderRepository stakeHolderRepository;
    private final StoricoModificaHelper storicoModificaHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final IMonitoraggioPrevenzioneRepository monitoraggioPrevenzioneRepository;

    private final IMisuraPrevenzioneEventoRischioStakeholderRepository misuraPrevenzioneEventoRischioStakeholderRepository;
    private final IMisuraPrevenzioneEventoRischioIndicatoreRepository misuraPrevenzioneEventoRischioIndicatoreRepository;

    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    public MisuraPrevenzioneEventoRischioServiceImpl(
        IMisuraPrevenzioneEventoRischioRepository prevenzioneEventoRischioRepository,
        IObiettivoPrevenzioneCorruzioneTrasparenzaRepository obiettivoPrevenzioneCorruzioneTrasparenzaRepository,
        IEventoRischioRepository eventoRischioRepository,
        IMisuraPrevenzioneEventoRischioMapper prevenzioneEventoRischioMapper,
        IIndicatoreRepository indicatoreRepository,
        IStakeHolderRepository stakeHolderRepository,
        StoricoModificaHelper storicoModificaHelper, ApplicationEventPublisher eventPublisher, IMonitoraggioPrevenzioneRepository monitoraggioPrevenzioneRepository, IMisuraPrevenzioneEventoRischioStakeholderRepository misuraPrevenzioneEventoRischioStakeholderRepository, IMisuraPrevenzioneEventoRischioIndicatoreRepository misuraPrevenzioneEventoRischioIndicatoreRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository) {
        this.misuraPrevenzioneEventoRischioRepository = prevenzioneEventoRischioRepository;
        this.obiettivoPrevenzioneCorruzioneTrasparenzaRepository = obiettivoPrevenzioneCorruzioneTrasparenzaRepository;
        this.eventoRischioRepository = eventoRischioRepository;
        this.prevenzioneEventoRischioMapper = prevenzioneEventoRischioMapper;
        this.indicatoreRepository = indicatoreRepository;
        this.stakeHolderRepository = stakeHolderRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.eventPublisher = eventPublisher;
        this.monitoraggioPrevenzioneRepository = monitoraggioPrevenzioneRepository;
        this.misuraPrevenzioneEventoRischioStakeholderRepository = misuraPrevenzioneEventoRischioStakeholderRepository;
        this.misuraPrevenzioneEventoRischioIndicatoreRepository = misuraPrevenzioneEventoRischioIndicatoreRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MisuraPrevenzioneEventoRischioDTO saveOrUpdate(MisuraPrevenzioneEventoRischioDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La richiesta non può essere null");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        try {
            // Mappo DTO -> Entity
            MisuraPrevenzioneEventoRischio entity = prevenzioneEventoRischioMapper.toEntity(dto, context);

            // Imposto manualmente le relazioni usando getReferenceById (lazy proxy - no query)
            entity.setEventoRischio(eventoRischioRepository.getReferenceById(dto.getIdEventoRischio()));

            if (dto.getIdObiettivoPrevenzioneCorruzioneTrasparenza() != null){
                entity.setObiettivoPrevenzioneCorruzioneTrasparenza(
                    obiettivoPrevenzioneCorruzioneTrasparenzaRepository.getReferenceById(dto.getIdObiettivoPrevenzioneCorruzioneTrasparenza())
                );
            }


            // Sincronizzazione Indicatori e Stakeholder (OneToMany)
            syncIndicatori(entity, dto.getIndicatori());
            syncStakeholder(entity, dto.getStakeholders());
            syncMonitoraggioPrevenzione(entity, dto.getMonitoraggioPrevenzione());

            log.info("MisuraPrevenzioneEventoRischio salvata con successo: id={}", entity.getId());
            // Salvataggio
            MisuraPrevenzioneEventoRischio savedEntity = misuraPrevenzioneEventoRischioRepository.save(entity);

            // Salva storico modifica se presente

                storicoModificaHelper.salvaStoricoSePresente(dto, savedEntity.getEventoRischio().getSezione23().getId(), dto.getIdPiao(), Sezione.SEZIONE_23);
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getObiettivoPrevenzioneCorruzioneTrasparenza().getSezione23().getId(),Sezione.SEZIONE_23.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getObiettivoPrevenzioneCorruzioneTrasparenza().getSezione23().getId())
                        .codTipologiaFK(Sezione.SEZIONE_23.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }

            return prevenzioneEventoRischioMapper.toDto(savedEntity, new CycleAvoidingMappingContext());



        } catch (Exception e) {
            log.error("Errore saveOrUpdate MisuraPrevenzioneEventoRischio id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore saveOrUpdate MisuraPrevenzioneEventoRischio", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrUpdateAll(List<MisuraPrevenzioneEventoRischioDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            log.debug("Lista MisuraPrevenzioneEventoRischio vuota o null, skip salvataggio batch");
            return;
        }

        log.info("Salvataggio batch di {} MisuraPrevenzioneEventoRischio", dtos.size());

        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();
            List<MisuraPrevenzioneEventoRischio> entitiesToSave = new ArrayList<>();

            for (MisuraPrevenzioneEventoRischioDTO dto : dtos) {
                if (dto == null) {
                    log.warn("MisuraPrevenzioneEventoRischioDTO nullo nella lista, skip");
                    continue;
                }

                // Mappo DTO -> Entity
                MisuraPrevenzioneEventoRischio entity = prevenzioneEventoRischioMapper.toEntity(dto, context);

                // Imposto manualmente le relazioni usando getReferenceById (lazy proxy - no query)
                entity.setEventoRischio(eventoRischioRepository.getReferenceById(dto.getIdEventoRischio()));

                if (dto.getIdObiettivoPrevenzioneCorruzioneTrasparenza() != null){
                    entity.setObiettivoPrevenzioneCorruzioneTrasparenza(
                        obiettivoPrevenzioneCorruzioneTrasparenzaRepository.getReferenceById(dto.getIdObiettivoPrevenzioneCorruzioneTrasparenza())
                    );
                }


                // Sincronizzazione Indicatori e Stakeholder (OneToMany)
                syncIndicatori(entity, dto.getIndicatori());
                syncStakeholder(entity, dto.getStakeholders());
                syncMonitoraggioPrevenzione(entity, dto.getMonitoraggioPrevenzione());

                entitiesToSave.add(entity);
            }

            // Salvataggio batch con saveAll di JPA
            List<MisuraPrevenzioneEventoRischio> savedEntities = misuraPrevenzioneEventoRischioRepository.saveAll(entitiesToSave);

            log.info("Batch salvataggio completato: {} MisuraPrevenzioneEventoRischio salvate", savedEntities.size());

        } catch (Exception e) {
            log.error("Errore durante il salvataggio batch delle MisuraPrevenzioneEventoRischio: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio batch delle MisuraPrevenzioneEventoRischio", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MisuraPrevenzioneEventoRischioDTO> getAllByEventoRischio(Long idEventoRischio) {
        if (idEventoRischio == null) {
            throw new IllegalArgumentException("L'ID dell'EventoRischio non può essere nullo");
        }

        List<MisuraPrevenzioneEventoRischioDTO> response;
        try {
            CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

            // getReferenceById restituisce un proxy JPA
            EventoRischio eventoRischio = eventoRischioRepository.getReferenceById(idEventoRischio);

            // Recuperiamo tutte le prevenzioni associate a quell'evento rischio
            List<MisuraPrevenzioneEventoRischio> entities = misuraPrevenzioneEventoRischioRepository.findByEventoRischioId(eventoRischio.getId());

            // Mappiamo entity → DTO
            response = entities.stream()
                .map(entity -> prevenzioneEventoRischioMapper.toDto(entity, context))
                .toList();



        } catch (Exception e) {
            log.error("Errore recupero MisuraPrevenzioneEventoRischio per EventoRischio id={} : {}", idEventoRischio, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle MisuraPrevenzioneEventoRischio per EventoRischio", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id,
                           String campiModificati,
                           Long idPiao,
                           String testoSezione,
                           String updatedByNameSurname,
                           String updatedByRole,
                           String statoSezione) {

        if (id == null) {
            throw new IllegalArgumentException("L'ID della MisuraPrevenzioneEventoRischio non può essere nullo");
        }

        try {
            Optional<MisuraPrevenzioneEventoRischio> existing = misuraPrevenzioneEventoRischioRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare una MisuraPrevenzioneEventoRischio non esistente con id={}", id);
                throw new RuntimeException("MisuraPrevenzioneEventoRischio non trovata con id: " + id);
            }

            MisuraPrevenzioneEventoRischio misura = existing.get();

            // Evento prima della cancellazione (coerenza con gli altri)
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(MisuraPrevenzioneEventoRischio.class, misura));

            LocalDateTime deactivationTime = LocalDateTime.now();

            //SOFT delete relazione con Monitoraggio
            monitoraggioPrevenzioneRepository.softDeleteByMisuraPrevenzioneId(id,deactivationTime);

            // Soft delete join MISURA <-> INDICATORE
            misuraPrevenzioneEventoRischioIndicatoreRepository
                .softDeleteByMisuraPrevenzioneEventoRischioId(id, deactivationTime);

            //  Soft delete join MISURA <-> STAKEHOLDER
            misuraPrevenzioneEventoRischioStakeholderRepository
                .softDeleteByMisuraPrevenzioneEventoRischioId(id, deactivationTime);



            // Soft delete misura
            misuraPrevenzioneEventoRischioRepository.softDeleteById(id, deactivationTime);

            Long idSezione23 = misura.getEventoRischio().getSezione23().getId();

            // Storico modifica
            if (campiModificati != null && !campiModificati.isBlank() && idPiao != null) {
                BaseDTO dto = BaseDTO.builder()
                    .campiModificati(campiModificati)
                    .idPiao(idPiao)
                    .updatedByNameSurname(updatedByNameSurname)
                    .updatedByRole(updatedByRole)
                    .testoSezione(testoSezione)
                    .statoSezione(statoSezione)
                    .build();

                storicoModificaHelper.salvaStoricoSePresente(dto, idSezione23, idPiao, Sezione.SEZIONE_23);
            }

            // Storico stato sezione
            if (statoSezione != null && !statoSezione.isBlank()) {
                String statoDb = StoricoStatoSezioneUtils.getStato(
                    storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(idSezione23, Sezione.SEZIONE_23.name())
                );

                String statoReq = StatoEnum.fromDescrizione(statoSezione).name();

                if (!statoReq.equals(statoDb)) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder()
                            .statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build()
                            )
                            .idEntitaFK(idSezione23)
                            .codTipologiaFK(Sezione.SEZIONE_23.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build()
                    );
                }
            }

            // Evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(misura));

            log.info("MisuraPrevenzioneEventoRischio con id={} cancellata con successo", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione di MisuraPrevenzioneEventoRischio id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new MisuraPrevenzioneEventoRischio(), e));
            throw new RuntimeException("Errore durante la cancellazione di MisuraPrevenzioneEventoRischio", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByEventoRischio(Long idEventoRischio, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione) {
        if (idEventoRischio == null) {
            throw new IllegalArgumentException("L'ID dell'EventoRischio non può essere nullo");
        }

        try {
            // Recupero tutte le MisuraPrevenzioneEventoRischio associate all'EventoRischio
            List<MisuraPrevenzioneEventoRischio> misure = misuraPrevenzioneEventoRischioRepository.findByEventoRischioId(idEventoRischio);

            if (misure.isEmpty()) {
                log.info("Nessuna MisuraPrevenzioneEventoRischio trovata per EventoRischio id={}", idEventoRischio);
                return;
            }

            // Cancellazione da Postgres
            misuraPrevenzioneEventoRischioRepository.deleteByEventoRischioId(idEventoRischio, LocalDateTime.now());

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
                storicoModificaHelper.salvaStoricoSePresente(dto, misure.get(0).getEventoRischio().getSezione23().getId(), idPiao, Sezione.SEZIONE_23);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank() && !misure.isEmpty()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(misure.get(0).getEventoRischio().getSezione23().getId(), Sezione.SEZIONE_23.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(misure.get(0).getEventoRischio().getSezione23().getId())
                            .codTipologiaFK(Sezione.SEZIONE_23.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            log.info("MisuraPrevenzioneEventoRischio per EventoRischio id={} cancellate con successo (totale: {})",
                idEventoRischio, misure.size());

        } catch (Exception e) {
            log.error("Errore durante la cancellazione delle MisuraPrevenzioneEventoRischio per EventoRischio id={}: {}",
                idEventoRischio, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione delle MisuraPrevenzioneEventoRischio per EventoRischio", e);
        }
    }

    /**
     * Sincronizza la lista di Indicatori della MisuraPrevenzioneEventoRischio.
     * Gli indicatori esistono già nel DB, va solo settato il riferimento alla misura.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni.
     */
    private void syncIndicatori(MisuraPrevenzioneEventoRischio parent, List<MisuraPrevenzioneEventoRischioIndicatoreDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setIndicatori(new ArrayList<>());
            return;
        }

        List<MisuraPrevenzioneEventoRischioIndicatore> entities = new ArrayList<>();

        for (MisuraPrevenzioneEventoRischioIndicatoreDTO dto : dtoList) {
            MisuraPrevenzioneEventoRischioIndicatore entity = MisuraPrevenzioneEventoRischioIndicatore.builder()
                .id(dto.getId())
                .misuraPrevenzioneEventoRischio(parent)
                .indicatore(indicatoreRepository.getReferenceById(dto.getIndicatore().getId()))
                .build();
            entities.add(entity);
        }

        parent.setIndicatori(entities);
    }

    /**
     * Sincronizza la lista di StakeHolders della MisuraPrevenzioneEventoRischio.
     * Gli stakeholder esistono già nel DB, va solo settato il riferimento alla misura.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni.
     */
    private void syncStakeholder(MisuraPrevenzioneEventoRischio parent, List<MisuraPrevenzioneEventoRischioStakeholderDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setStakeholders(new ArrayList<>());
            return;
        }

        List<MisuraPrevenzioneEventoRischioStakeholder> entities = new ArrayList<>();

        for (MisuraPrevenzioneEventoRischioStakeholderDTO dto : dtoList) {
            if (dto == null || dto.getStakeholder() == null || dto.getStakeholder().getId() == null) {
                log.warn("StakeholderDTO nullo o senza ID, skip");
                continue;
            }

            MisuraPrevenzioneEventoRischioStakeholder entity = MisuraPrevenzioneEventoRischioStakeholder.builder()
                .id(dto.getId())
                .misuraPrevenzioneEventoRischio(parent)
                .stakeholder(stakeHolderRepository.getReferenceById(dto.getStakeholder().getId()))
                .build();
            entities.add(entity);
        }

        parent.setStakeholders(entities);
    }

    /**
     * Sincronizza la lista di Indicatori della MisuraPrevenzioneEventoRischio.
     * Gli indicatori esistono già nel DB, va solo settato il riferimento alla misura.
     * Hibernate gestisce automaticamente insert/update/delete delle relazioni.
     */
    private void syncMonitoraggioPrevenzione(MisuraPrevenzioneEventoRischio parent, List<MonitoraggioPrevenzioneDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            parent.setMonitoraggioPrevenzione(new ArrayList<>());
            return;
        }

        List<MonitoraggioPrevenzione> entities = new ArrayList<>();

        for (MonitoraggioPrevenzioneDTO dto : dtoList) {
            MonitoraggioPrevenzione entity = MonitoraggioPrevenzione.builder()
                .id(dto.getId())
                .misuraPrevenzioneEventoRischio(parent)
                .descrizione(dto.getDescrizione())
                .tipologia(dto.getTipologia())
                .tempistiche(dto.getTempistiche())
                .responsabile(dto.getResponsabile())
                .build();
            entities.add(entity);
        }

        parent.setMonitoraggioPrevenzione(entities);
    }
}
