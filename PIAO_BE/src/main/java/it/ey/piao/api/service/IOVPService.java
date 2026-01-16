package it.ey.piao.api.service;

import it.ey.dto.OVPDTO;

import java.util.List;

public interface IOVPService {
    public OVPDTO saveOrUpdate(OVPDTO ovp);
    public List<OVPDTO> findAllOVPBySezione(Long idSezione);
    public void deleteById(Long id);
}
