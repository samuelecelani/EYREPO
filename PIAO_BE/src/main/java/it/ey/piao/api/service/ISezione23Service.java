package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione23DTO;
import it.ey.entity.Sezione23;

public interface ISezione23Service {
    Sezione23DTO getOrCreateSezione23(PiaoDTO piao);
    Sezione23DTO saveOrUpdate(Sezione23DTO request);
    Sezione23DTO richiediValidazione(Long id);
    Sezione23DTO loadMongoDataSezione23(Sezione23DTO sezione23);
    Sezione23DTO findByPiao(PiaoDTO piao);
    void saveMongoDataSezione23(Sezione23DTO response, Sezione23DTO request);
}
