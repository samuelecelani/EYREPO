package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione331DTO;

public interface ISezione331Service {

    Sezione331DTO getOrCreateSezione331(PiaoDTO piao);
    public void  saveOrUpdate(Sezione331DTO request);
    Sezione331DTO richiediValidazione(Long id);
    Sezione331DTO findByPiao(Long idPiao);
}
