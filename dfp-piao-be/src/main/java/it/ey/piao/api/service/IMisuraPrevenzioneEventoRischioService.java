package it.ey.piao.api.service;

import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;

import java.util.List;

public interface IMisuraPrevenzioneEventoRischioService {
    MisuraPrevenzioneEventoRischioDTO saveOrUpdate(MisuraPrevenzioneEventoRischioDTO prevenzioneEventoRischio);

    void saveOrUpdateAll(List<MisuraPrevenzioneEventoRischioDTO> prevenzioniEventoRischio);

    List<MisuraPrevenzioneEventoRischioDTO> getAllByEventoRischio(Long idEventoRischio);

    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);

    void deleteByEventoRischio(Long idEventoRischio, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);

}
