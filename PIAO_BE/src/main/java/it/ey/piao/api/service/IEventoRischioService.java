package it.ey.piao.api.service;

import it.ey.dto.EventoRischioDTO;

import java.util.List;

public interface IEventoRischioService {

    EventoRischioDTO saveOrUpdate(EventoRischioDTO eventoRischio);

    void saveOrUpdateAll(List<EventoRischioDTO> eventiRischio);

    List<EventoRischioDTO> getAllByAttivitaSensibile(Long idAttivitaSensibile);

    void deleteById(Long id);

    void deleteByAttivitaSensibile(Long idAttivitaSensibile);

    EventoRischioDTO loadMongoData(EventoRischioDTO eventoRischio);

    void saveMongoData(EventoRischioDTO request);
}
