package it.ey.piao.api.service;


import it.ey.dto.AdditionalInfoDTO;

public interface IAdditionalInfoService {
    public AdditionalInfoDTO findByExternalId(Long externalId);
    public  AdditionalInfoDTO  save(AdditionalInfoDTO additionalInfoDTO);
    public AdditionalInfoDTO findById(String id);
}
