package it.ey.piao.bff.service.impl;

import it.ey.dto.AnagraficaDTO;
import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.dto.external.AmministrazioneExternalPPDTO;
import it.ey.dto.external.DocumentoPiaoExternalPPDTO;
import it.ey.dto.utils.PageResponse;
import it.ey.enums.WebServiceType;
import it.ey.externaldto.UserProfileDto;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IExternalService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ExternalServiceImpl implements IExternalService {

    private static final Logger log = LoggerFactory.getLogger(ExternalServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;


    public ExternalServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<DocumentoPiaoExternalPPDTO>>> getPiaoAndAllegati(Long idPiao, String denominazione, String codePa) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/piao/external/findAllPiaoPubblicati");
        List<String> params = new ArrayList<>();
        if (idPiao != null) {
            params.add("idPiao=" + idPiao);
        }
        if (StringUtils.isNotBlank(denominazione)) {
            params.add("denominazione=" + denominazione);
        }
        if (StringUtils.isNotBlank(codePa)) {
            params.add("codePa=" + codePa);
        }
        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        return webClientService.get(
                url.toString(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<DocumentoPiaoExternalPPDTO>>() {}
            )
            .doOnNext(response -> log.info("PIAO consultato: {}", response))
            .map(lista -> {
                GenericResponseDTO<List<DocumentoPiaoExternalPPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(lista);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            }).onErrorResume(e -> {
                GenericResponseDTO<List<DocumentoPiaoExternalPPDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<AmministrazioneExternalPPDTO>>> getAmministrazioniServiziComuni(Boolean isBIP) {
        if (isBIP) {
            return getAmministrazioniFromBip();
        }
        return getAmministrazioniFromApi();
    }

    private Mono<GenericResponseDTO<List<AmministrazioneExternalPPDTO>>> getAmministrazioniFromApi() {
        log.info("Recupero amministrazioni da API (Anagrafica)");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/anagrafica",
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<AnagraficaDTO>>() {}
            )
            .doOnNext(response -> log.info("Anagrafiche recuperate: {} elementi", response != null ? response.size() : 0))
            .map(anagraficaList -> {
                GenericResponseDTO<List<AmministrazioneExternalPPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                List<AmministrazioneExternalPPDTO> amministrazioni = Collections.emptyList();
                if (anagraficaList != null) {
                    amministrazioni = anagraficaList.stream()
                        .map(ana -> AmministrazioneExternalPPDTO.builder()
                            .codiceIpa(ana.getCodiceIPA())
                            .denominazione(ana.getDenominazioneEnte())
                            .tipologia(ana.getTipologiaPA())
                            .build())
                        .toList();
                }
                finalResponse.setData(amministrazioni);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero amministrazioni da API: {}", e.getMessage(), e);
                return buildErrorResponse(e);
            });
    }

    private Mono<GenericResponseDTO<List<AmministrazioneExternalPPDTO>>> getAmministrazioniFromBip() {
        log.info("Recupero amministrazioni da BIP");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/public/amministrazione/list",
                WebServiceType.BIP,
                headers,
                new ParameterizedTypeReference<PageResponse<UserProfileDto.AmministrazioneDto>>() {}
            )
            .doOnNext(response -> log.info("Amministrazioni BIP recuperate: {} elementi",
                response != null && response.getContent() != null ? response.getContent().size() : 0))
            .map(pageResponse -> {
                GenericResponseDTO<List<AmministrazioneExternalPPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                List<AmministrazioneExternalPPDTO> amministrazioni = Collections.emptyList();
                if (pageResponse != null && pageResponse.getContent() != null) {
                    amministrazioni = pageResponse.getContent().stream()
                        .map(amm -> AmministrazioneExternalPPDTO.builder()
                            .codiceIpa(amm.getIpaCode())
                            .denominazione(amm.getName())
                            .build())
                        .toList();
                }
                finalResponse.setData(amministrazioni);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero amministrazioni da BIP: {}", e.getMessage(), e);
                return buildErrorResponse(e);
            });
    }

    private Mono<GenericResponseDTO<List<AmministrazioneExternalPPDTO>>> buildErrorResponse(Throwable e) {
        GenericResponseDTO<List<AmministrazioneExternalPPDTO>> errorResponse = new GenericResponseDTO<>();
        errorResponse.setStatus(new Status());
        errorResponse.getStatus().setSuccess(Boolean.FALSE);
        errorResponse.setError(new Error());
        errorResponse.getError().setMessageError(e.getMessage());
        return Mono.just(errorResponse);
    }
}
