package it.ey.piao.api.service;

import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;

import java.util.List;

public interface IObiettivoPrevenzioneCorruzioneTrasparenzaService {

    ObiettivoPrevenzioneCorruzioneTrasparenzaDTO  saveOrUpdate(ObiettivoPrevenzioneCorruzioneTrasparenzaDTO dto);

   void  saveAll(List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> dtos);

    List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> getAllBySezione23(Long idSezione23);


    void deleteById(Long id);
}

