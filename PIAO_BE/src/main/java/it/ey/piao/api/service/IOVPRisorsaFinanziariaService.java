package it.ey.piao.api.service;

import it.ey.dto.OVPRisorsaFinanziariaDTO;

import java.util.List;

public interface IOVPRisorsaFinanziariaService {

    List<OVPRisorsaFinanziariaDTO> saveOrUpdate(List<OVPRisorsaFinanziariaDTO> request);
}
