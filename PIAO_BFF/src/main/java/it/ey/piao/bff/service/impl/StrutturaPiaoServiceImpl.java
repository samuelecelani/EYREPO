package it.ey.piao.bff.service.impl;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStrutturaPiaoService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        String uri = UriComponentsBuilder.fromPath("/struttura/piao")
            .queryParamIfPresent("idPiao", Optional.ofNullable(idPiao))
            .build()
            .toUriString(); // -> "/struttura/piao" oppure "/struttura/piao?idPiao=123"

        return webClientService.get(
                uri,
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<StrutturaPiaoDTO>>() {})
            .map(res -> {
                GenericResponseDTO<List<StrutturaPiaoDTO>> response = new GenericResponseDTO<>();
                response.setData(res);
                response.setStatus(Status.builder().isSuccess(true).build());
                return response;
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<StrutturaValidazioneDTO>>> getStrutturaValidazione(Long idPiao) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String uri = UriComponentsBuilder.fromPath("/struttura/validazione")
            .queryParam("idPiao", idPiao)
            .build()
            .toUriString();

        return webClientService.get(
                uri,
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<StrutturaValidazioneDTO>>() {})
            .map(res -> {
                GenericResponseDTO<List<StrutturaValidazioneDTO>> response = new GenericResponseDTO<>();
                response.setData(res);
                response.setStatus(Status.builder().isSuccess(true).build());
                return response;
            });
    }
}
