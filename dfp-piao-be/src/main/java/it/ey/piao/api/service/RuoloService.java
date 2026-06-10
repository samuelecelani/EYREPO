package it.ey.piao.api.service;

import it.ey.dto.RuoloDTO;

import java.util.List;

public interface RuoloService {
    List<RuoloDTO> findByTipologia(List<String> tipologia);
}
