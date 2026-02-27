package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione22DTO;

public interface ISezione22Service {
    Sezione22DTO getOrCreateSezione22(PiaoDTO piao);
    void saveOrUpdate(Sezione22DTO request);
    Sezione22DTO richiediValidazione(Long id);
    Sezione22DTO findByIdPiao(Long idPiao);
    Sezione22DTO loadMongoDataSezione22(Sezione22DTO sezione22);
    void saveMongoDataSezione22(Sezione22DTO response, Sezione22DTO request);
}
