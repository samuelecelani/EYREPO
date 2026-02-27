package it.ey.piao.api.service;

import it.ey.dto.ProceduraDTO;

import java.util.List;

public interface IProceduraService {
    public List<ProceduraDTO> getProcedure (Long idSezione1);
    public ProceduraDTO save (ProceduraDTO request);
}
