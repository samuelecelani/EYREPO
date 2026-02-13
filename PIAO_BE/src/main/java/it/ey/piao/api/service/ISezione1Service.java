package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione1DTO;

public interface ISezione1Service {
    Sezione1DTO getOrCreateSezione1(PiaoDTO piao);
    Sezione1DTO saveOrUpdate(Sezione1DTO request);
    Sezione1DTO richiediValidazione(Long id);
    Sezione1DTO loadMongoDataSezione1(Sezione1DTO sezione1);
    Sezione1DTO findByPiao(PiaoDTO piao);
    void saveMongoDataSezione1(Sezione1DTO response, Sezione1DTO request);
}
