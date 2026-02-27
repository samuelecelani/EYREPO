package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione1DTO;

public interface ISezione1Service {
    Sezione1DTO getOrCreateSezione1(PiaoDTO piao);
    void saveOrUpdate(Sezione1DTO request);
    Sezione1DTO richiediValidazione(Long id);
    Sezione1DTO loadMongoDataSezione1(Sezione1DTO sezione1);
    Sezione1DTO findByPiao(Long idPiao);
    void saveMongoDataSezione1(Long sezione1Id, Sezione1DTO request);
}
