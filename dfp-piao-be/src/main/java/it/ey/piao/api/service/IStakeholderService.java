package it.ey.piao.api.service;

import it.ey.dto.StakeHolderDTO;

import java.util.List;

public interface IStakeholderService {
    public List<StakeHolderDTO> findByidPiao(Long idPiao);
    public StakeHolderDTO save(StakeHolderDTO dto);
    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole,boolean forceDelete, String statoSezione);


}
