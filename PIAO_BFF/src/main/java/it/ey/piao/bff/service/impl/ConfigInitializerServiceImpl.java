package it.ey.piao.bff.service.impl;

import it.ey.dto.ConfigFeInitializerDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.piao.bff.service.IConfigInitializerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConfigInitializerServiceImpl implements IConfigInitializerService {
    @Value("${internal.service.url.api}")
    private String baseUrl;
    @Value("${internal.service.isProduction}")
    private boolean isProduction;

    @Override
    public Mono<GenericResponseDTO<ConfigFeInitializerDTO>> configInitializer(){
        GenericResponseDTO<ConfigFeInitializerDTO> response = new GenericResponseDTO<>();
        response.setStatus(Status.builder().isSuccess(true).build());
        response.setData(ConfigFeInitializerDTO.builder().apiEndpoint(baseUrl).isProduction(isProduction).build());
        return Mono.just(response);
    }

}
