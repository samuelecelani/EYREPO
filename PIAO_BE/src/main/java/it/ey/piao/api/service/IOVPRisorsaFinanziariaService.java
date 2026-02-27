package it.ey.piao.api.service;

import it.ey.dto.OVPRisorsaFinanziariaDTO;

import java.util.List;

public interface IOVPRisorsaFinanziariaService {

    void saveOrUpdate(List<OVPRisorsaFinanziariaDTO> request, Long idOVP);
}
