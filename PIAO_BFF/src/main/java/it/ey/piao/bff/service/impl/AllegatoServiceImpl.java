
package it.ey.piao.bff.service.impl;

import it.ey.dto.AllegatoDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.global.exception.CustomBusinessException;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAllegatoService;
import it.ey.piao.bff.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AllegatoServiceImpl implements IAllegatoService {

    private static final Logger log = LoggerFactory.getLogger(AllegatoServiceImpl.class);

    private final S3Service s3Service;
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    public AllegatoServiceImpl(S3Service s3Service, WebClientService webClientService) {
        this.s3Service = s3Service;
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<AllegatoDTO>> insertAllegato(AllegatoDTO allegato, FilePart filePart) {
        if (filePart == null) {
            return Mono.error(new CustomBusinessException("File mancante"));
        }

        log.info("[SERVICE] Upload allegato: {} - file: {}", allegato, filePart.filename());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return s3Service.uploadFile(filePart) // Mono<String> -> fileKey generato su MinIO
            .flatMap(fileKey -> {
                allegato.setCodDocumento(fileKey);
                return webClientService.post(
                        "/allegato/save",
                        webServiceType,
                        allegato,
                        headers,
                        AllegatoDTO.class
                    )
                    .map(res -> {
                        GenericResponseDTO<AllegatoDTO> response = new GenericResponseDTO<>();
                        response.setData(res);
                        response.setStatus(Status.builder().isSuccess(true).build());
                        return response;
                    });
            })
            .onErrorMap(ex -> new CustomBusinessException("Errore durante upload/salvataggio allegato: " + ex.getMessage()));
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteAllegato(Long allegatoId, String fileKey) {
        try {
            if (allegatoId == null || fileKey == null || fileKey.isBlank()) {
                throw new IllegalArgumentException("ID allegato o fileKey mancanti");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return s3Service.deleteFile(fileKey) // Mono<Void>
                .then(webClientService.delete("/allegato/delete/" + allegatoId, webServiceType, headers))
                .map(res -> {
                    GenericResponseDTO<Void> response = new GenericResponseDTO<>();
                    response.setStatus(Status.builder().isSuccess(true).build());
                    return response;
                })
                .onErrorMap(ex -> new CustomBusinessException("Errore durante l'eliminazione dell'allegato: " + ex.getMessage()));
        } catch (Exception ex) {
            return Mono.error(new CustomBusinessException("Errore durante l'eliminazione dell'allegato: " + ex.getMessage()));
        }
    }

    @Override
    public Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegati(String codTipologia, String codTipologiaAllegato, Long idPiao) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return webClientService.get(
                "/allegato/by-tipologia?codTipologia=" + codTipologia +
                    "&codTipologiaAllegato=" + codTipologiaAllegato +
                    "&idPiao=" + idPiao,
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<AllegatoDTO>>() {}
            )
            .flatMap(allegati ->
                Flux.fromIterable(allegati)
                    .flatMap(allegato ->
                        s3Service.generatePresignedUrl(allegato.getCodDocumento())
                            .map(url -> {
                                allegato.setDownloadUrl(url);
                                return allegato;
                            })
                    )
                    .collectList()
            )
            .map(updatedList -> {
                GenericResponseDTO<List<AllegatoDTO>> response = new GenericResponseDTO<>();
                response.setData(updatedList);
                response.setStatus(Status.builder().isSuccess(true).build());
                return response;
            })
            .onErrorMap(ex -> new CustomBusinessException("Errore nel recupero allegati: " + ex.getMessage()));
    }
}

