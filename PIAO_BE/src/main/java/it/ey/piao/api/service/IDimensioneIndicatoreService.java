package it.ey.piao.api.service;

import it.ey.dto.DimensioneIndicatoreDTO;

import java.util.List;

public interface IDimensioneIndicatoreService {
    List<DimensioneIndicatoreDTO> findByCodTipologiaFK(String codTipologiaFK);
}

