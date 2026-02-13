package it.ey.piao.api.service;

import it.ey.dto.PrioritaPoliticaDTO;

import java.util.List;

public interface IPrioritaPoliticaService {
    public List<PrioritaPoliticaDTO> findByidSezione1(Long idSezione1);
    public List<PrioritaPoliticaDTO> findByPiaoId(Long piaoId);
    public PrioritaPoliticaDTO save(PrioritaPoliticaDTO dto);
}
