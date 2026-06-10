package it.ey.piao.api.service;

import it.ey.dto.AttivitaDTO;

public interface IAttivitaService {

    AttivitaDTO saveOrUpdate(AttivitaDTO dto);

    AttivitaDTO getByExternalId(Long externalId);

    void deleteByExternalId(Long externalId);
}
