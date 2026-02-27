package it.ey.piao.api.service.impl;

import it.ey.dto.PrioritaPoliticaDTO;
import it.ey.entity.PrioritaPolitica;
import it.ey.piao.api.mapper.PrioritaPoliticaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IPrioritaPoliticaRepository;
import it.ey.piao.api.service.IPrioritaPoliticaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PrioritaPoliticaServiceImpl implements IPrioritaPoliticaService {

    private static final Logger log = LoggerFactory.getLogger(PrioritaPoliticaServiceImpl.class);

    private final IPrioritaPoliticaRepository prioritaPoliticaRepository;
    private final PrioritaPoliticaMapper prioritaPoliticaMapper;

    public PrioritaPoliticaServiceImpl(IPrioritaPoliticaRepository prioritaPoliticaRepository, PrioritaPoliticaMapper prioritaPoliticaMapper) {
        this.prioritaPoliticaRepository = prioritaPoliticaRepository;
        this.prioritaPoliticaMapper = prioritaPoliticaMapper;
    }

    @Override
    public List<PrioritaPoliticaDTO> findByidSezione1(Long idSezione1) {
        if (idSezione1 == null) {
            throw new IllegalArgumentException("idSezione1 è obbligatorio");
        }
        try {
            log.debug("Ricerca PrioritaPolitiche per idSezione1={}", idSezione1);
            return prioritaPoliticaMapper.toDtoList(prioritaPoliticaRepository.findBySezione1Id(idSezione1),new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in findByidSezione1 (PrioritaPolitica) idSezione1={}: {}", idSezione1, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero delle Priorità Politiche", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findByidSezione1 (PrioritaPolitica) idSezione1={}: {}", idSezione1, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Priorità Politiche", e);
        }
    }

    @Override
    public PrioritaPoliticaDTO save(PrioritaPoliticaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("PrioritaPoliticaDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio PrioritaPolitica: {}", dto);
            PrioritaPolitica entity = prioritaPoliticaMapper.toEntity(dto,new CycleAvoidingMappingContext());
            PrioritaPolitica saved = prioritaPoliticaRepository.save(entity);
            return prioritaPoliticaMapper.toDto(saved,new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in save (PrioritaPolitica): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio della Priorità Politica", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (PrioritaPolitica): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio della Priorità Politica", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrioritaPoliticaDTO> findByPiaoId(Long piaoId) {
        if (piaoId == null) {
            throw new IllegalArgumentException("piaoId è obbligatorio");
        }
        try {
            log.debug("Ricerca PrioritaPolitiche per piaoId={}", piaoId);
            return prioritaPoliticaMapper.toDtoList(prioritaPoliticaRepository.findByPiaoId(piaoId), new CycleAvoidingMappingContext());
        } catch (DataAccessException dae) {
            log.error("Errore DB in findByPiaoId (PrioritaPolitica) piaoId={}: {}", piaoId, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero delle Priorità Politiche per PIAO", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findByPiaoId (PrioritaPolitica) piaoId={}: {}", piaoId, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Priorità Politiche per PIAO", e);
        }
    }
}
