package it.ey.piao.api.service;

import it.ey.dto.AttivitaSensibileDTO;

import java.util.List;

public interface IAttivitaSensibileService {

    void saveOrUpdate(AttivitaSensibileDTO attivitaSensibile);

    void saveOrUpdateAll(List<AttivitaSensibileDTO> attivitaSensibili);

    List<AttivitaSensibileDTO> getAllBySezione23(Long idSezione23);
    AttivitaSensibileDTO loadMongoData(AttivitaSensibileDTO attivitaSensibile);
    void deleteById(Long id);
}
