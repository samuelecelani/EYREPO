package it.ey.piao.api.service.impl;

import it.ey.dto.StakeHolderDTO;
import it.ey.entity.Piao;
import it.ey.entity.StakeHolder;

import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.IStakeHolderRepository;

import it.ey.piao.api.service.IStakeholderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StakeholderServiceImpl implements IStakeholderService {

    private static final Logger log = LoggerFactory.getLogger(StakeholderServiceImpl.class);

    private final IStakeHolderRepository stakeholderRepository;
    private final GenericMapper genericMapper;

    public StakeholderServiceImpl(IStakeHolderRepository stakeholderRepository, GenericMapper genericMapper) {
        this.stakeholderRepository = stakeholderRepository;
        this.genericMapper = genericMapper;
    }

    @Override
    public List<StakeHolderDTO> findByidPiao(Long idPiao) {
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao è obbligatorio");
        }
        try {
            log.debug("Ricerca Stakeholder per idSezione1={}", idPiao);
            return stakeholderRepository.findByPiao(Piao.builder().id(idPiao).build())
                .stream()
                .map(e -> genericMapper.map(e, StakeHolderDTO.class))
                .toList();
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
            StakeHolder entity = genericMapper.map(dto, StakeHolder.class);
            StakeHolder saved = stakeholderRepository.save(entity);
            return genericMapper.map(saved, StakeHolderDTO.class);
        } catch (DataAccessException dae) {
            log.error("Errore DB in save (Stakeholder): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello Stakeholder", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (Stakeholder): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello Stakeholder", e);
        }
    }
}

