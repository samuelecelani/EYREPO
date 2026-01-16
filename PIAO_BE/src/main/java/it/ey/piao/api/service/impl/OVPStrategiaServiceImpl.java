package it.ey.piao.api.service.impl;

import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import it.ey.entity.OVPStrategia;
import it.ey.entity.OVPStrategiaIndicatore;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.repository.IOVPStrategiaIndicatoreRepository;
import it.ey.piao.api.repository.IOVPStrategiaRepository;
import it.ey.piao.api.service.IOVPStrategiaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OVPStrategiaServiceImpl implements IOVPStrategiaService {

    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaServiceImpl.class);

    private final IOVPStrategiaRepository iovpStrategiaRepository;
    private final IOVPStrategiaIndicatoreRepository iovpStrategiaIndicatoreRepository;
    private final GenericMapper genericMapper;

    public OVPStrategiaServiceImpl(IOVPStrategiaRepository iovpStrategiaRepository, GenericMapper genericMapper, IOVPStrategiaIndicatoreRepository iovpStrategiaIndicatoreRepository) {
        this.iovpStrategiaRepository = iovpStrategiaRepository;
        this.genericMapper = genericMapper;
        this.iovpStrategiaIndicatoreRepository = iovpStrategiaIndicatoreRepository;
    }

    @Override
    public OVPStrategiaDTO save(OVPStrategiaDTO request) {
        if(request == null || request.getOvp() == null){
            throw new IllegalArgumentException("OVPStrategiaDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio OVPStrategiaDTO: {}", request);
            OVPStrategia entity = genericMapper.map(request, OVPStrategia.class);
            OVPStrategia saved = iovpStrategiaRepository.save(entity);
            return genericMapper.map(saved, OVPStrategiaDTO.class);
        } catch (DataAccessException dae) {
            log.error("Errore DB in save (OVPStrategia): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello OVPStrategia", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (OVPStrategia): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello OVPStrategia", e);
        }
    }

    @Override
    public List<OVPStrategiaDTO> save(List<OVPStrategiaDTO> request) {
        if(request == null || request.isEmpty()){
            throw new IllegalArgumentException("List<OVPStrategiaDTO> è obbligatorio");
        }
        try {
            List<OVPStrategiaDTO> response = new ArrayList<>();
            request.forEach(dto->{
                response.add(this.save(dto));
            });
            return response;
        } catch (DataAccessException dae) {
            log.error("Errore DB in save List (OVPStrategia): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio Lista dello OVPStrategia", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save List (OVPStrategia): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio Lista dello OVPStrategia", e);
        }
    }

    @Override
    public OVPStrategiaIndicatoreDTO saveIndicatore(OVPStrategiaIndicatoreDTO request) {
        if(request == null || request.getOvpStrategia() == null || request.getIndicatoreDTO() == null){
            throw new IllegalArgumentException("OVPStrategiaIndicatoreDTO è obbligatorio");
        }
        try {
            log.debug("Salvataggio OVPStrategiaIndicatoreDTO: {}", request);
            OVPStrategiaIndicatore entity = genericMapper.map(request, OVPStrategiaIndicatore.class);
            OVPStrategiaIndicatore saved = iovpStrategiaIndicatoreRepository.save(entity);
            return genericMapper.map(saved, OVPStrategiaIndicatoreDTO.class);
        } catch (DataAccessException dae) {
            log.error("Errore DB in save (OVPStrategiaIndicatore): {}", dae.getMessage(), dae);
            throw new RuntimeException("Errore di persistenza durante il salvataggio dello OVPStrategiaIndicatore", dae);
        } catch (Exception e) {
            log.error("Errore inatteso in save (OVPStrategiaIndicatore): {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante il salvataggio dello OVPStrategiaIndicatore", e);
        }
    }
}
