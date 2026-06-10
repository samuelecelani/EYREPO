package it.ey.piao.api.service;

import it.ey.dto.DatiPubblicatiDTO;

import java.util.List;

public interface IDatiPubblicatiService {
 public DatiPubblicatiDTO saveOrUpdate(DatiPubblicatiDTO dto);
    List<DatiPubblicatiDTO> getAllByObbligoLeggeId(Long idObbligoLegge);
    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);
    void saveAll(List <DatiPubblicatiDTO> request);
}
