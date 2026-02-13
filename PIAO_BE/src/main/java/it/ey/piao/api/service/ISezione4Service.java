package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione4DTO;

public interface ISezione4Service {
    Sezione4DTO getOrCreateSezione4(PiaoDTO piao);
    Sezione4DTO saveOrUpdate(Sezione4DTO request);
    Sezione4DTO richiediValidazione(Long id);
    Sezione4DTO findByPiao(PiaoDTO piao);
    Sezione4DTO loadMongoDataSezione4(Sezione4DTO sezione4);
    void saveMongoDataSezione4(Sezione4DTO response, Sezione4DTO request);
}
