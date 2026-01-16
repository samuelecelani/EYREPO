package it.ey.piao.api.service.impl;

import it.ey.dto.AreaOrganizzativaDTO;
import it.ey.entity.AreaOrganizzativa;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.IAreaOrganizzativaRepository;
import it.ey.piao.api.service.IAreaOrganizzativaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AreaOrganizzativaServiceImpl implements IAreaOrganizzativaService {

    private static final Logger log = LoggerFactory.getLogger(AreaOrganizzativaServiceImpl.class);

    private final IAreaOrganizzativaRepository areaOrganizzativaRepository;
    private final GenericMapper genericMapper;

    public AreaOrganizzativaServiceImpl(IAreaOrganizzativaRepository areaOrganizzativaRepository, GenericMapper genericMapper) {
        this.areaOrganizzativaRepository = areaOrganizzativaRepository;
        this.genericMapper = genericMapper;
    }

    @Override
    public List<AreaOrganizzativaDTO> findByidSezione1(Long idSezione1) {
        if (idSezione1 == null) {
            throw new IllegalArgumentException("idSezione1 è obbligatorio");
        }
        try {
            log.debug("Ricerca AreeOrganizzative per idSezione1={}", idSezione1);
            return areaOrganizzativaRepository.findBySezione1Id(idSezione1)
                .stream()
                .map(a -> genericMapper.map(a, AreaOrganizzativaDTO.class))
                .toList();
        } catch (DataAccessException dae) {
            log.error("Errore DB in findByidSezione1 idSezione1={}: {}", idSezione1, dae.getMessage(), dae);
            throw new RuntimeException("Errore di accesso ai dati durante il recupero delle Aree Organizzative", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in findByidSezione1 idSezione1={}: {}", idSezione1, e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero delle Aree Organizzative", e);
        }
    }

    @Override
    public AreaOrganizzativaDTO save(AreaOrganizzativaDTO areaOrganizzativa) {
        if (areaOrganizzativa == null) {
            throw new IllegalArgumentException("AreaOrganizzativaDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio AreaOrganizzativa: {}", areaOrganizzativa);
            AreaOrganizzativa entity = genericMapper.map(areaOrganizzativa, AreaOrganizzativa.class);
            AreaOrganizzativa saved = areaOrganizzativaRepository.save(entity);
            return genericMapper.map(saved, AreaOrganizzativaDTO.class);
        } catch (DataAccessException dae) {
            log.error("Errore DB in save AreaOrganizzativa: {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dell'Area Organizzativa", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save AreaOrganizzativa: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dell'Area Organizzativa", e);
        }
    }
}
