
package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.global.exception.CustomBusinessException;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAllegatoService;
import it.ey.piao.bff.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Collections;
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
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

            if (!allegato.getIsDoc()) {
                return DataBufferUtils.join(filePart.content())
                    .flatMap(dataBuffer -> {
                        try {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            allegato.setCodDocumento(allegato.getCodDocumento() + allegato.getType());
                            String base64 = Base64.getEncoder().encodeToString(bytes);
                            allegato.setLogo(
                                new LogoDTO(null,null,Collections.singletonList(
                                    PropertyDTO.builder()
                                        .key(filePart.filename())
                                        .value(base64)
                                        .build() ))
                            );

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

                        } finally {
                            DataBufferUtils.release(dataBuffer);
                        }
                    })
                    .onErrorMap(ex -> new CustomBusinessException(
                        "Errore durante upload/salvataggio allegato: " + ex.getMessage()
                    ));
            }

            log.info("[SERVICE] Upload allegato: {} - file: {}", allegato, filePart.filename());

            return s3Service.uploadFile(filePart) // Mono<String> (fileKey)
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
                .onErrorMap(ex -> new CustomBusinessException(
                    "Errore durante upload/salvataggio allegato: " + ex.getMessage()
                ));
        }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteAllegato(Long allegatoId, String fileKey, boolean isDoc) {
        try {
            if (allegatoId == null || fileKey == null || fileKey.isBlank()) {
                throw new IllegalArgumentException("ID allegato o fileKey mancanti");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            // Se non è un documento, eliminiamo prima da S3, poi dal backend (che eliminerà anche da MongoDB)
            Mono<Void> deleteOperation = !isDoc
                ? Mono.empty()
                : s3Service.deleteFile(fileKey);

            return deleteOperation
                .then(webClientService.delete("/allegato/delete/" + allegatoId + "?isDoc=" + isDoc, webServiceType, headers))
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
    public Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegati(String codTipologia, String codTipologiaAllegato, Long idPiao, boolean isDoc) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return webClientService.get(
                "/allegato/by-tipologia?codTipologia=" + codTipologia +
                    "&codTipologiaAllegato=" + codTipologiaAllegato +
                    "&idPiao=" + idPiao +
                    "&isDoc=" + isDoc,
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<AllegatoDTO>>() {}
            )
            .flatMap(allegati ->
                Flux.fromIterable(allegati)
                    .flatMap(allegato -> {
                        // Se è un documento, generiamo il presigned URL da S3
                        if (isDoc) {
                            return s3Service.generatePresignedUrl(allegato.getCodDocumento())
                                .map(url -> {
                                    allegato.setDownloadUrl(url);
                                    return allegato;
                                });
                        } else {
                            // Se non è un documento, il base64 è già presente nell'allegato
                            return Mono.just(allegato);
                        }
                    })
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

