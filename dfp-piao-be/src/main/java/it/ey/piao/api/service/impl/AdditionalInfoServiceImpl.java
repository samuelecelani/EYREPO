package it.ey.piao.api.service.impl;

import it.ey.dto.AdditionalInfoDTO;
import it.ey.piao.api.repository.mongo.IAdditionalInfoRepository;
import it.ey.entity.AdditionalInfo;
import it.ey.piao.api.service.IAdditionalInfoService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AdditionalInfoServiceImpl implements IAdditionalInfoService {

@Autowired
IAdditionalInfoRepository additionalInfoRepository;


    @Override
    public AdditionalInfoDTO findByExternalId(Long externalId) {
        return new AdditionalInfoDTO(additionalInfoRepository.findByExternalId(externalId));
    }

    @Override
    public AdditionalInfoDTO save(AdditionalInfoDTO additionalInfoDTO) {
        return new AdditionalInfoDTO( additionalInfoRepository.save(new AdditionalInfo(additionalInfoDTO)));
    }

    @Override
    public AdditionalInfoDTO findById(String id) {
        return additionalInfoRepository.findById(id)
            .map(AdditionalInfoDTO::new)
            .orElseThrow(() -> new EntityNotFoundException("AdditionalInfo non trovato con id: " + id));
    }


}

