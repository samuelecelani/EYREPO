package it.ey.piao.api.service.impl;

import it.ey.dto.OVPDTO;
import it.ey.dto.OVPRisorsaFinanziariaDTO;
import it.ey.entity.OVP;
import it.ey.entity.OVPRisorsaFinanziaria;
import it.ey.piao.api.mapper.OVPRisorsaFinanziariaMapper;
import it.ey.piao.api.repository.IOVPRisorsaFinanziariaRepository;
import it.ey.piao.api.repository.OVPRepository;
import it.ey.piao.api.service.IOVPRisorsaFinanziariaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class OVPRisorsaFinanziariaServiceImpl implements IOVPRisorsaFinanziariaService {

    private final IOVPRisorsaFinanziariaRepository iovpRisorsaFinanziariaRepository;
    private final OVPRepository ovpRepository;
    private final OVPRisorsaFinanziariaMapper ovpRisorsaFinanziariaMapper;

    private static final Logger log = LoggerFactory.getLogger(OVPRisorsaFinanziariaServiceImpl.class);

    public OVPRisorsaFinanziariaServiceImpl(IOVPRisorsaFinanziariaRepository iovpRisorsaFinanziariaRepository, OVPRepository ovpRepository, OVPRisorsaFinanziariaMapper ovpRisorsaFinanziariaMapper) {
        this.iovpRisorsaFinanziariaRepository = iovpRisorsaFinanziariaRepository;
        this.ovpRepository = ovpRepository;
        this.ovpRisorsaFinanziariaMapper = ovpRisorsaFinanziariaMapper;
    }

    @Override
    public void saveOrUpdate(List<OVPRisorsaFinanziariaDTO> request, Long idOVP) {
        try {
            //recupero l'ovp
            OVP ovp = ovpRepository.getReferenceById(idOVP);
            // Mappo i DTO in entità
            List<OVPRisorsaFinanziaria> entities = request.stream()
                .map(dto -> {
                    OVPRisorsaFinanziaria entity = ovpRisorsaFinanziariaMapper.toEntity(dto);

                    // Carico l'OVP
                    entity.setOvp(ovp);

                    return entity;
                })
                .toList();

            // Salvo le entità
            iovpRisorsaFinanziariaRepository.saveAll(entities);


        } catch (Exception e) {
            log.error("Errore durante saveOrUpdate OVPRisorsaFinanziariaServiceImpl: {}", e.getMessage(), e);

            throw new RuntimeException("Errore durante il salvataggio o aggiornamento dell'OVPRisorsaFinanziariaServiceImpl", e);
        }
    }
}
