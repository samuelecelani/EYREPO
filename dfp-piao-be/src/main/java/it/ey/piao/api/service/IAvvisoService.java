package it.ey.piao.api.service;

import it.ey.dto.AvvisoDTO;

import java.util.List;

public interface IAvvisoService {
    List<AvvisoDTO> getAll();
    AvvisoDTO getById(Long id);
    AvvisoDTO create(AvvisoDTO avvisoDTO);
    AvvisoDTO update(Long id, AvvisoDTO avvisoDTO);
    void delete(Long id);
}

