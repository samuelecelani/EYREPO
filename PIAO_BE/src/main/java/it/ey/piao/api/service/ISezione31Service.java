package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione23DTO;
import it.ey.dto.Sezione31DTO;

public interface ISezione31Service
{
    Sezione31DTO getOrCreateSezione31(PiaoDTO piao);
    void saveOrUpdate(Sezione31DTO request);
    Sezione31DTO richiediValidazione(Long id);
    Sezione31DTO findByPiao(Long idPiao);
}
