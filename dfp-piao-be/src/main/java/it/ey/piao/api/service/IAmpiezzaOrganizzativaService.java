package it.ey.piao.api.service;

import it.ey.dto.AmpiezzaOrganizzativaDTO;

import java.util.List;

public interface IAmpiezzaOrganizzativaService {
    List<AmpiezzaOrganizzativaDTO> findByIdSezione31(Long idSezione31);
    AmpiezzaOrganizzativaDTO save(AmpiezzaOrganizzativaDTO ampiezzaOrganizzativa);
    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);
}
