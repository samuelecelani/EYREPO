package it.ey.piao.api.service.impl;

import it.ey.dto.AreaOrganizzativaDTO;
import it.ey.entity.AreaOrganizzativa;
import it.ey.piao.api.mapper.AreaOrganizzativaMapper;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import it.ey.piao.api.repository.IAreaOrganizzativaRepository;
import it.ey.piao.api.service.IAreaOrganizzativaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class AreaOrganizzativaServiceImpl implements IAreaOrganizzativaService {

    private static final Logger log = LoggerFactory.getLogger(AreaOrganizzativaServiceImpl.class);

    private final IAreaOrganizzativaRepository areaOrganizzativaRepository;
    private final AreaOrganizzativaMapper areaOrganizzativaMapper;

    public AreaOrganizzativaServiceImpl(IAreaOrganizzativaRepository areaOrganizzativaRepository, AreaOrganizzativaMapper areaOrganizzativaMapper) {
        this.areaOrganizzativaRepository = areaOrganizzativaRepository;
        this.areaOrganizzativaMapper = areaOrganizzativaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaOrganizzativaDTO> findByidSezione1(Long idSezione1) {

            log.debug("Ricerca AreeOrganizzative per idSezione1={}", idSezione1);
            return areaOrganizzativaMapper.toDtoList(areaOrganizzativaRepository.findBySezione1Id(idSezione1),new CycleAvoidingMappingContext());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public AreaOrganizzativaDTO save(AreaOrganizzativaDTO areaOrganizzativa) {
        if (areaOrganizzativa == null) {
            throw new IllegalArgumentException("AreaOrganizzativaDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio AreaOrganizzativa: {}", areaOrganizzativa);
            AreaOrganizzativa entity = areaOrganizzativaMapper.toEntity(areaOrganizzativa,new CycleAvoidingMappingContext());
            AreaOrganizzativa saved = areaOrganizzativaRepository.save(entity);
            return areaOrganizzativaMapper.toDto(saved,new CycleAvoidingMappingContext());
        } catch (Exception e) {
            log.error("Errore inatteso in save AreaOrganizzativa: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dell'Area Organizzativa", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AreaOrganizzativaDTO> findByPiaoId(Long piaoId) {
            return areaOrganizzativaMapper.toDtoList(areaOrganizzativaRepository.findByPiaoId(piaoId), new CycleAvoidingMappingContext());

    }
}
