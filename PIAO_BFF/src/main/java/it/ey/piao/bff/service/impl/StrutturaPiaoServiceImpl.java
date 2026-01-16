package it.ey.piao.bff.service.impl;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStrutturaPiaoService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class StrutturaPiaoServiceImpl implements IStrutturaPiaoService {
    private final WebServiceType  webServiceType;
    private final WebClientService webClientService;

    public StrutturaPiaoServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }
    @Override
    public Mono<GenericResponseDTO<List<StrutturaPiaoDTO>>> getStrutturaPiao(Long idPiao) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return webClientService.get("/struttura/piao?idPiao=" +  idPiao, webServiceType, headers, new ParameterizedTypeReference<List<StrutturaPiaoDTO>>() {})
            .map(res -> {
                GenericResponseDTO<List<StrutturaPiaoDTO>> response = new GenericResponseDTO<>();
                response.setData(res);
                response.setStatus(Status.builder().isSuccess(true).build());
                return response;
            });


    }


}
