package it.ey.piao.api.service;

import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione1DTO;

public interface ISezione1Service {
    public Sezione1DTO getOrCreateSezione1(PiaoDTO piao);
    public Sezione1DTO saveOrUpdate(Sezione1DTO request);
    public Sezione1DTO richiediValidazione(Long id);
}
