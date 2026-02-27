package it.ey.piao.bff.service;




import it.ey.dto.AdditionalInfoDTO;
import reactor.core.publisher.Mono;


public interface IAdditionalInfoService {
    public Mono<AdditionalInfoDTO> findByExternalId(Long externalId);
    public Mono<AdditionalInfoDTO> save(AdditionalInfoDTO additionalInfoDTO);
    public Mono<AdditionalInfoDTO> findById(String id);
}
