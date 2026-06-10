package it.ey.piao.api.service;

import it.ey.dto.AttivitaFormativeDTO;

public interface IAttivitaFormativeService
{
    AttivitaFormativeDTO saveOrUpdate(AttivitaFormativeDTO attivitaFormativeDTO);

    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);
}
