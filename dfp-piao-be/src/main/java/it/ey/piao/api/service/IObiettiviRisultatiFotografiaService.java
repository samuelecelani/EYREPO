package it.ey.piao.api.service;

import it.ey.dto.*;

import java.util.List;

public interface IObiettiviRisultatiFotografiaService {

    ObiettiviRisultatiFotografiaDTO saveOrUpdate(ObiettiviRisultatiFotografiaDTO obiettiviRisultati);

    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);

    List<ObiettiviRisultatiFotografiaDTO> getObiettiviRisultatiByIdSezione332(Long idSezione332);

    List<ObiettiviRisultatiFotografiaDTO> getFotografieFormazioneByIdSezione332(Long idSezione332);

}
