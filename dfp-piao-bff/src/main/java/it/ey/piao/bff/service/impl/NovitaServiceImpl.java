package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.dto.external.NewsDetailExternalDTO;
import it.ey.dto.external.NewsSearchResponseExternalDTO;
import it.ey.dto.external.NewsTipologieResponseExternalDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.mapper.NovitaMapper;
import it.ey.piao.bff.service.INovitaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NovitaServiceImpl implements INovitaService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(NovitaServiceImpl.class);

    public NovitaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.OPENCMS;
    }

    @Override
    public Mono<GenericResponseDTO<List<NovitaTipologiaDTO>>> getNewsTipologie(String locale) {
        log.info("Recupero tipologie news da OpenCms, locale={}", locale);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String url = "/opencms/handle/news-tipologie?locale=" + (locale != null ? locale : "it");

        return webClientService.get(url, webServiceType, headers, NewsTipologieResponseExternalDTO.class, true)
            .doOnNext(response -> log.info("Tipologie news ricevute: {} items", response.getItems() != null ? response.getItems().size() : 0))
            .map(externalResponse -> {
                List<NovitaTipologiaDTO> tipologie = NovitaMapper.toNovitaTipologiaDTOList(externalResponse);
                GenericResponseDTO<List<NovitaTipologiaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(tipologie);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero tipologie news: {}", e.getMessage(), e);
                GenericResponseDTO<List<NovitaTipologiaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<NovitaPaginatedDTO>> searchNews(String keyword, String tipologia,
                                                                    Boolean inEvidenza, String startDate,
                                                                    String sort, Integer page, Integer limit,
                                                                    String locale) {
        log.info("Ricerca news OpenCms: keyword={}, tipologia={}, page={}, limit={}", keyword, tipologia, page, limit);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        if (keyword != null && !keyword.isBlank()) formData.add("keyword", keyword);
        if (tipologia != null && !tipologia.isBlank()) formData.add("tipologia", tipologia);
        if (inEvidenza != null) formData.add("inEvidenza", inEvidenza.toString());
        if (startDate != null && !startDate.isBlank()) formData.add("startDate", startDate);
        formData.add("sort", sort != null ? sort : "date_desc");
        formData.add("page", String.valueOf(page != null ? page : 1));
        formData.add("limit", String.valueOf(limit != null ? limit : 10));
        formData.add("locale", locale != null ? locale : "it");

        return webClientService.postFormUrlEncoded("/opencms/handle/news-search", webServiceType, formData, headers, NewsSearchResponseExternalDTO.class, true)
            .doOnNext(response -> log.info("Ricerca news completata: total={}", response.getTotal()))
            .map(externalResponse -> {
                NovitaPaginatedDTO paginatedDTO = NovitaMapper.toNovitaPaginatedDTO(externalResponse);
                GenericResponseDTO<NovitaPaginatedDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(paginatedDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore ricerca news: {}", e.getMessage(), e);
                GenericResponseDTO<NovitaPaginatedDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<NovitaDTO>> getNewsDetail(String id) {
        log.info("Recupero dettaglio news OpenCms: id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String url = "/opencms/handle/news-detail?id=" + id;

        return webClientService.get(url, webServiceType, headers, NewsDetailExternalDTO.class, true)
            .doOnNext(response -> log.info("Dettaglio news ricevuto: id={}", response.getId()))
            .map(externalDetail -> {
                NovitaDTO novitaDTO = NovitaMapper.toNovitaDetailDTO(externalDetail);
                GenericResponseDTO<NovitaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(novitaDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero dettaglio news: {}", e.getMessage(), e);
                GenericResponseDTO<NovitaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
