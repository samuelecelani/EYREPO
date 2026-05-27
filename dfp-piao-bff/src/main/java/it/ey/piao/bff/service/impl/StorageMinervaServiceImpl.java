package it.ey.piao.bff.service.impl;

import it.ey.dto.StorageMinervaDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStorageMinervaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StorageMinervaServiceImpl implements IStorageMinervaService
{
    private static final Logger log = LoggerFactory.getLogger(StorageMinervaServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    public StorageMinervaServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<StorageMinervaDTO> saveOrUpdate(StorageMinervaDTO dto)
    {
        log.info("Proxy saveOrUpdate StorageMinerva: identitafk={}, codtipologiafk={}, codiceipa={}",
            dto != null ? dto.getIdentitafk() : null,
            dto != null ? dto.getCodtipologiafk() : null,
            dto != null ? dto.getCodiceipa() : null);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.post("/storage-minerva", webServiceType, dto, headers, StorageMinervaDTO.class)
            .doOnNext(r -> log.info("StorageMinerva saveOrUpdate OK: id={}", r != null ? r.getId() : null))
            .doOnError(e -> log.error("Errore saveOrUpdate StorageMinerva: {}", e.getMessage(), e));
    }
}

