package it.ey.sync.service;

import it.ey.sync.dto.AllegatoPiaoDTO;

import java.util.List;
import java.util.Optional;

public interface AllegatoPiaoService {

    List<AllegatoPiaoDTO> findAll();

    Optional<AllegatoPiaoDTO> findById(Integer id);

    AllegatoPiaoDTO save(AllegatoPiaoDTO dto);

    void deleteById(Integer id);
}

