package it.ey.piao.api.service.impl;

import it.ey.dto.StakeHolderDTO;
import it.ey.entity.Piao;
import it.ey.entity.StakeHolder;

import it.ey.piao.api.mapper.StakeHolderMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IStakeHolderRepository;

import it.ey.piao.api.repository.PiaoRepository;
import it.ey.piao.api.service.IStakeholderService;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.piao.api.service.event.TransactionFailureEvent;
import it.ey.piao.api.service.event.TransactionSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StakeholderServiceImpl implements IStakeholderService {

    private static final Logger log = LoggerFactory.getLogger(StakeholderServiceImpl.class);

    private final IStakeHolderRepository stakeholderRepository;
    private final StakeHolderMapper stakeHolderMapper;
    private final PiaoRepository piaoRepository;
    private final ApplicationEventPublisher eventPublisher;


    public StakeholderServiceImpl(IStakeHolderRepository stakeholderRepository, StakeHolderMapper stakeHolderMapper, PiaoRepository piaoRepository,ApplicationEventPublisher eventPublisher) {
        this.stakeholderRepository = stakeholderRepository;
        this.stakeHolderMapper = stakeHolderMapper;
        this.piaoRepository = piaoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public List<StakeHolderDTO> findByidPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }
        try {
            log.debug("Ricerca Stakeholder per idSezione1={}", idPiao);
            return stakeHolderMapper.toDtoList(stakeholderRepository.findByIdPiao(idPiao),new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in findByidSezione1 (Stakeholder) idSezione1={}: {}", idPiao, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero degli Stakeholder", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findByidSezione1 (Stakeholder) idSezione1={}: {}", idPiao, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero degli Stakeholder", e);
        }
    }

    @Override
    public StakeHolderDTO save(StakeHolderDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("StakeHolderDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio Stakeholder: {}", dto);
            StakeHolder entity = stakeHolderMapper.toEntity(dto,new CycleAvoidingMappingContext());
            entity.setPiao(piaoRepository.getReferenceById(dto.getIdPiao()));
            StakeHolder saved = stakeholderRepository.save(entity);
            return stakeHolderMapper.toDto(saved,new CycleAvoidingMappingContext());
        }  catch (Exception e) {
            log.error("Errore inatteso in save (Stakeholder): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello Stakeholder", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            // Recupero l'entità prima della cancellazione per eventuali rollback/log
            Optional<StakeHolder> existing = stakeholderRepository.findById(id);
            if (existing.isEmpty()) {
                log.warn("Tentativo di cancellare uno StakeHolder non esistente con id={}", id);
                throw new RuntimeException("StakeHolder non trovato con id: " + id);
            }

            // Pubblico evento prima della cancellazione (opzionale, se vuoi tracciare)
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(StakeHolder.class, existing.get()));

            // Cancellazione dal DB principale
            stakeholderRepository.deleteById(id);

            // Se ci fossero dati in MongoDB collegati allo stakeholder
            // esempio:
            // mongoStakeholderRepository.deleteByExternalId(id);

            // Pubblico evento di successo
            eventPublisher.publishEvent(new TransactionSuccessEvent<>(existing.get()));

            log.info("StakeHolder con id={} cancellato con successo", id);
        } catch (Exception e) {
            log.error("Errore durante la cancellazione dello StakeHolder id={}: {}", id, e.getMessage(), e);
            eventPublisher.publishEvent(new TransactionFailureEvent<>(new StakeHolder(), e));
            throw new RuntimeException("Errore durante la cancellazione dello StakeHolder", e);
        }

    }
}

