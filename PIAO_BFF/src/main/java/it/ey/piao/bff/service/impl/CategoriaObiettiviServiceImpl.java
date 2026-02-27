package it.ey.piao.bff.service.impl;

import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ICategoriaObiettiviService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CategoriaObiettiviServiceImpl implements ICategoriaObiettiviService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(CategoriaObiettiviServiceImpl.class);

    public CategoriaObiettiviServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<CategoriaObiettiviDTO>> saveOrUpdate(CategoriaObiettiviDTO request) {
        log.info("Richiesta salvataggio/modifica CategoriaObiettivi");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.post("/categoria-obiettivi/save", webServiceType, request, headers, CategoriaObiettiviDTO.class)
            .doOnNext(response -> log.info("CategoriaObiettivi salvato/modificato: {}", response))
            .map(categoriaObiettiviDTO -> {
                GenericResponseDTO<CategoriaObiettiviDTO> finalResponse = new GenericResponseDTO<>();

                if (categoriaObiettiviDTO == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica CategoriaObiettivi");
                }

                finalResponse.setData(categoriaObiettiviDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica CategoriaObiettivi: {}", e.getMessage(), e);

                GenericResponseDTO<CategoriaObiettiviDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<CategoriaObiettiviDTO>>> getAllBySezione4(Long idSezione4) {
        log.info("Richiesta recupero CategoriaObiettivi per Sezione4 con id={}", idSezione4);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/categoria-obiettivi/sezione4/" + idSezione4, webServiceType, headers,
                new ParameterizedTypeReference<List<CategoriaObiettiviDTO>>() {})
            .doOnNext(response ->
                log.info("CategoriaObiettivi recuperati: {} elementi", response != null ? response.size() : 0))
            .map(categoriaObiettiviDTOList -> {
                GenericResponseDTO<List<CategoriaObiettiviDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(categoriaObiettiviDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero CategoriaObiettivi per Sezione4 id={}: {}", idSezione4, e.getMessage(), e);

                GenericResponseDTO<List<CategoriaObiettiviDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Richiesta cancellazione CategoriaObiettivi con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/categoria-obiettivi/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("CategoriaObiettivi con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione CategoriaObiettivi con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
