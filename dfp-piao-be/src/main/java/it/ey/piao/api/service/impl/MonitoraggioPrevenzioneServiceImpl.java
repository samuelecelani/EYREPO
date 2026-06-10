package it.ey.piao.api.service.impl;

import it.ey.dto.BaseDTO;
import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.entity.*;
import it.ey.enums.Sezione;
import it.ey.enums.StatoEnum;
import it.ey.piao.api.mapper.IMisuraPrevenzioneEventoRischioMapper;
import it.ey.piao.api.mapper.MonitoraggioPrevenzioneMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IMisuraPrevenzioneEventoRischioRepository;
import it.ey.piao.api.repository.IMonitoraggioPrevenzioneRepository;
import it.ey.piao.api.repository.IStoricoStatoSezioneRepository;
import it.ey.piao.api.service.IMonitoraggioPrevenzioneService;
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
import java.util.List;
import java.util.Optional;

@Service
public class MonitoraggioPrevenzioneServiceImpl implements IMonitoraggioPrevenzioneService
{
    private final IMonitoraggioPrevenzioneRepository monitoraggioPrevenzioneRepository;
    private final MonitoraggioPrevenzioneMapper monitoraggioPrevenzioneMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository;
    private final IStoricoStatoSezioneRepository storicoStatoSezioneRepository;

    private final StoricoModificaHelper storicoModificaHelper;

    private static final Logger log = LoggerFactory.getLogger(MonitoraggioPrevenzioneServiceImpl.class);
    private final IMisuraPrevenzioneEventoRischioMapper misuraPrevenzioneEventoRischioMapper;

    public MonitoraggioPrevenzioneServiceImpl(IMonitoraggioPrevenzioneRepository monitoraggioPrevenzioneRepository,
                                              MonitoraggioPrevenzioneMapper monitoraggioPrevenzioneMapper,
                                              ApplicationEventPublisher eventPublisher,
                                              IMisuraPrevenzioneEventoRischioRepository misuraPrevenzioneEventoRischioRepository, IStoricoStatoSezioneRepository storicoStatoSezioneRepository,
                                              StoricoModificaHelper storicoModificaHelper,
                                              IMisuraPrevenzioneEventoRischioMapper misuraPrevenzioneEventoRischioMapper)
    {
        this.monitoraggioPrevenzioneRepository = monitoraggioPrevenzioneRepository;
        this.monitoraggioPrevenzioneMapper = monitoraggioPrevenzioneMapper;
        this.eventPublisher = eventPublisher;
        this.misuraPrevenzioneEventoRischioRepository = misuraPrevenzioneEventoRischioRepository;
        this.storicoStatoSezioneRepository = storicoStatoSezioneRepository;
        this.storicoModificaHelper = storicoModificaHelper;
        this.misuraPrevenzioneEventoRischioMapper = misuraPrevenzioneEventoRischioMapper;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public MonitoraggioPrevenzioneDTO saveOrUpdate(MonitoraggioPrevenzioneDTO dto)
    {
        if(dto == null)
        {
            throw new IllegalArgumentException("La richiesta non puo' essere nulla");
        }

        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        MonitoraggioPrevenzioneDTO response;
        try {
            // DTO in entity JPA
            MonitoraggioPrevenzione entity = monitoraggioPrevenzioneMapper.toEntity(dto,context);

            // Relazione con MisuraPrevenzioneEventoRischio
            if (dto.getIdMisuraPrevenzioneEventoRischio() != null) {
                entity.setMisuraPrevenzioneEventoRischio(misuraPrevenzioneEventoRischioRepository.getReferenceById(dto.getIdMisuraPrevenzioneEventoRischio()));
            }

            // Salvo l'entity principale nel DB relazionale
            MonitoraggioPrevenzione savedEntity = monitoraggioPrevenzioneRepository.save(entity);

            // Mappo l'entity salvata in DTO di risposta
            response = monitoraggioPrevenzioneMapper.toDto(savedEntity,context);

            if (dto.getCampiModificati() != null && !dto.getCampiModificati().isBlank() && dto.getIdPiao() != null ) {
                storicoModificaHelper.salvaStoricoSePresente(dto, savedEntity.getMisuraPrevenzioneEventoRischio().getEventoRischio().getSezione23().getId(), dto.getIdPiao(), Sezione.SEZIONE_23);
            }
            if (dto.getStatoSezione() != null && !dto.getStatoSezione().isBlank() && !StatoEnum.fromDescrizione(dto.getStatoSezione()).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(savedEntity.getMisuraPrevenzioneEventoRischio().getObiettivoPrevenzioneCorruzioneTrasparenza().getSezione23().getId(),Sezione.SEZIONE_23.name())))) {
                storicoStatoSezioneRepository.save(
                    StoricoStatoSezione.builder().statoSezione(
                            StatoSezione.builder()
                                .id(StatoEnum.fromDescrizione(dto.getStatoSezione()).getId())
                                .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                                .build())
                        .idEntitaFK(savedEntity.getMisuraPrevenzioneEventoRischio().getObiettivoPrevenzioneCorruzioneTrasparenza().getSezione23().getId())
                        .codTipologiaFK(Sezione.SEZIONE_23.name())
                        .testo(StatoEnum.fromDescrizione(dto.getStatoSezione()).getDescrizione())
                        .createdByNameSurname(dto.getUpdatedByNameSurname())
                        .createdByRole(dto.getUpdatedByRole())
                        .build());
            }

        } catch (Exception e) {
            log.error("Errore durante Save o update per MonitoraggioPrevenzione id={}: {}", dto.getId(), e.getMessage(), e);
            throw new RuntimeException("Errore durante il save o update del MonitoraggioPrevenzione", e);
        }

        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteById(Long id,  String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione)
    {
        if (id == null) {
            throw new IllegalArgumentException("L'ID del MonitoraggioPrevenzione non può essere nullo");
        }

        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback
            Optional<MonitoraggioPrevenzione> existing = monitoraggioPrevenzioneRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare un MonitoraggioPrevenzione non esistente con id={}", id);
                throw new RuntimeException("MonitoraggioPrevenzione non trovato con id: " + id);
            }

            // Cancellazione da Postgres
            monitoraggioPrevenzioneRepository.softDeleteById(id, LocalDateTime.now());

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
                storicoModificaHelper.salvaStoricoSePresente(dto, existing.get().getMisuraPrevenzioneEventoRischio().getEventoRischio().getSezione23().getId(), idPiao, Sezione.SEZIONE_23);
            }

            // Salva storico stato sezione dopo la cancellazione
            if (statoSezione != null && !statoSezione.isBlank()) {
                if (!StatoEnum.fromDescrizione(statoSezione).name().equals(StoricoStatoSezioneUtils.getStato(storicoStatoSezioneRepository.findByIdEntitaAndCodTipologia(existing.get().getMisuraPrevenzioneEventoRischio().getEventoRischio().getSezione23().getId(), Sezione.SEZIONE_23.name())))) {
                    storicoStatoSezioneRepository.save(
                        StoricoStatoSezione.builder().statoSezione(
                                StatoSezione.builder()
                                    .id(StatoEnum.fromDescrizione(statoSezione).getId())
                                    .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                                    .build())
                            .idEntitaFK(existing.get().getMisuraPrevenzioneEventoRischio().getEventoRischio().getSezione23().getId())
                            .codTipologiaFK(Sezione.SEZIONE_23.name())
                            .testo(StatoEnum.fromDescrizione(statoSezione).getDescrizione())
                            .createdByNameSurname(updatedByNameSurname)
                            .createdByRole(updatedByRole)
                            .build());
                }
            }

            log.info("MonitoraggioPrevenzione con id={} cancellato con successo", id);

        } catch (Exception e) {
            log.error("Errore durante la cancellazione del MonitoraggioPrevenzione id={}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Errore durante la cancellazione del MonitoraggioPrevenzione", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonitoraggioPrevenzioneDTO> getAllByMisuraPrevenzioneEventoRischio(Long idMisuraPrevenzioneEventoRischio)
    {
        if (idMisuraPrevenzioneEventoRischio == null) {
            throw new IllegalArgumentException("L'ID della misuraPrevenzioneEventoRischio non può essere nullo");
        }
        CycleAvoidingMappingContext context = new CycleAvoidingMappingContext();

        List<MonitoraggioPrevenzioneDTO> response;
        try {
            // Recupero il riferimento alla misuraPrevenzioneEventoRischio senza eseguire subito la query
            MisuraPrevenzioneEventoRischio misuraPrevenzioneEventoRischio = misuraPrevenzioneEventoRischioRepository.getReferenceById(idMisuraPrevenzioneEventoRischio);

            // Recupero tutte le misuraPrevenzioneEventoRischio associati al MonitoraggioPrevenzione
            List<MonitoraggioPrevenzione> entities = monitoraggioPrevenzioneRepository.getMonitoraggioByMisuraPrevenzioneEventoRischio(misuraPrevenzioneEventoRischio.getId());

            // Mapping Entity → DTO con arricchimento dei dati Mongo
            response = entities.stream()
                .map(entity -> {
                    // Mapping base JPA → DTO
                    MonitoraggioPrevenzioneDTO monitoraggioPrevenzioneDTO = monitoraggioPrevenzioneMapper.toDto(entity,context);
                    return monitoraggioPrevenzioneDTO;
                })
                .toList();

            // Pubblico evento di successo della transazione
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(response));

        } catch (Exception e) {
            log.error("Errore durante il recupero dei MonitoraggioPrevenzione per MisuraEventoRischio id={}: {}",
                idMisuraPrevenzioneEventoRischio, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dei MonitoraggioPrevenzione per MisuraEventoRischio", e);
        }

        return response;
    }
}
