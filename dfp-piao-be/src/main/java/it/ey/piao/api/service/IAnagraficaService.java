package it.ey.piao.api.service;

import it.ey.dto.AnagraficaDTO;

import java.util.List;

public interface IAnagraficaService {
    List<AnagraficaDTO> getAll();
    AnagraficaDTO save(AnagraficaDTO anagraficaDTO);
    AnagraficaDTO findByIdPiao(Long idPiao);
    List<AnagraficaDTO> search(String codiceIpa, String tipologia, String denominazione);
    List<String> getTipologie();
}
