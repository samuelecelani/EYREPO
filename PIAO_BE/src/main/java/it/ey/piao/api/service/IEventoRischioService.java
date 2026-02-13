package it.ey.piao.api.service;

import it.ey.dto.EventoRischioDTO;

import java.util.List;

public interface IEventoRischioService {

    EventoRischioDTO saveOrUpdate(EventoRischioDTO eventoRischio);

    List<EventoRischioDTO> getAllByAttivitaSensibile(Long idAttivitaSensibile);

    void deleteById(Long id);

    EventoRischioDTO loadMongoData(EventoRischioDTO eventoRischio);

    void saveMongoData(EventoRischioDTO response, EventoRischioDTO request);
}
