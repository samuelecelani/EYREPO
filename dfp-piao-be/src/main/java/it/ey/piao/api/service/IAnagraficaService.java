package it.ey.piao.api.service;

import it.ey.dto.AnagraficaDTO;

import java.util.List;

public interface IAnagraficaService {
    List<AnagraficaDTO> getAll();
    AnagraficaDTO save(AnagraficaDTO anagraficaDTO);
    AnagraficaDTO findByIdSezione1(Long idSezione1);
    List<AnagraficaDTO> search(String codiceIpa, String tipologia, String denominazione);
    List<String> getTipologie();
}
