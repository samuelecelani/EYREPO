package it.ey.piao.api.service;

import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;

import java.util.List;

public interface IMisuraPrevenzioneEventoRischioService {
    MisuraPrevenzioneEventoRischioDTO saveOrUpdate(MisuraPrevenzioneEventoRischioDTO prevenzioneEventoRischio);

    List<MisuraPrevenzioneEventoRischioDTO> getAllByEventoRischio(Long idEventoRischio);

    void deleteById(Long id);


}
