package it.ey.piao.api.service;

public interface IOrganoPoliticoService {

    void deleteById(Long id,
                    String campiModificati,
                    Long idPiao,
                    String testoSezione,
                    String updatedByNameSurname,
                    String updatedByRole,
                    String statoSezione);
}
