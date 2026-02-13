package it.ey.piao.api.service;

import it.ey.dto.AttivitaSensibileDTO;
import it.ey.dto.ObiettivoPrevenzioneDTO;
import it.ey.entity.AttivitaSensibile;

import java.util.List;

public interface IAttivitaSensibileService {

    AttivitaSensibileDTO saveOrUpdate(AttivitaSensibileDTO attivitaSensibile);


    List<AttivitaSensibileDTO> getAllBySezione23(Long idSezione23);


    void deleteById(Long id);
}
