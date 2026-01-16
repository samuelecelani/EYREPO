package it.ey.piao.api.service.impl;

import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.ProceduraDTO;
import it.ey.entity.OVPStrategia;
import it.ey.entity.Procedura;
import it.ey.entity.Sezione21;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.IProceduraRepository;
import it.ey.piao.api.service.IProceduraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProceduraServiceImpl implements IProceduraService {

    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaServiceImpl.class);


    private final IProceduraRepository proceduraRepository;
    private final GenericMapper genericMapper;

    public ProceduraServiceImpl (IProceduraRepository proceduraRepository, GenericMapper genericMapper){
        this.proceduraRepository = proceduraRepository;
        this.genericMapper = genericMapper;
    }

    @Override
    public List<ProceduraDTO> getProcedure(Long idSezione21) {
        try {
            return proceduraRepository.getProcedureBySezione21(Sezione21.builder().id(idSezione21).build())
                .stream().map(p -> genericMapper.map(p, ProceduraDTO.class)).toList();
        } catch (DataAccessException dae) {
            log.error("Errore DB in recupero (Procedura): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il recupero dello Procedura", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in recupero (Procedura): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero dello Procedura", e);
        }

    }

    @Override
    public ProceduraDTO save(ProceduraDTO request) {
        try {
            return genericMapper.map(proceduraRepository.save(genericMapper.map(request, Procedura.class)), ProceduraDTO.class);
        } catch (DataAccessException dae) {
            log.error("Errore DB in save (Procedura): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello Procedura", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (Procedura): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello Procedura", e);
        }
    }
}
