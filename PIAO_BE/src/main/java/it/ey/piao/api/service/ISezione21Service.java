package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione21DTO;

public interface ISezione21Service {
    public Sezione21DTO getOrCreateSezione21(PiaoDTO piao);
    public Sezione21DTO saveOrUpdate(Sezione21DTO request);
    public Sezione21DTO richiediValidazione(Long id);

}
