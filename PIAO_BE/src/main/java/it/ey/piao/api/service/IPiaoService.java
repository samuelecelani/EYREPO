package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;

import java.util.List;

public interface IPiaoService {
    public PiaoDTO getOrCreatePiao(PiaoDTO piao);
    public boolean redigiPiaoIsAllowed(String codPAFK);
    public List<PiaoDTO> getAllPiaoByCodPAFK(String codPAF);


}
