package it.ey.piao.api.service;

import it.ey.dto.StrutturaPiaoDTO;

import java.util.List;

public interface IStrutturaPiaoService {

    public List<StrutturaPiaoDTO> getAllStruttura(Long idPiao);
}
