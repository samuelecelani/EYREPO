package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione21DTO;

public interface ISezione21Service {
    Sezione21DTO getOrCreateSezione21(PiaoDTO piao);
    void saveOrUpdate(Sezione21DTO request);
    Sezione21DTO richiediValidazione(Long id);
    Sezione21DTO findByIdPiao(Long idPiao);
    Sezione21DTO loadMongoDataSezione21(Sezione21DTO sezione21);
    void saveMongoDataSezione21(Long sezione21Id, Sezione21DTO request);
}
