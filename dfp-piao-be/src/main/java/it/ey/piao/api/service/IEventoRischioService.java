package it.ey.piao.api.service;

import it.ey.dto.EventoRischioDTO;

import java.util.List;

public interface IEventoRischioService {

    EventoRischioDTO saveOrUpdate(EventoRischioDTO eventoRischio);

    void saveOrUpdateAll(List<EventoRischioDTO> eventiRischio);

    List<EventoRischioDTO> getAllByAttivitaSensibile(Long idAttivitaSensibile);

    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);

    void deleteByAttivitaSensibile(Long idAttivitaSensibile, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);

    EventoRischioDTO loadMongoData(EventoRischioDTO eventoRischio);

    void saveMongoData(EventoRischioDTO request);
}
