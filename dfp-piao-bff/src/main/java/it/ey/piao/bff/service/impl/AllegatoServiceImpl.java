
package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.enums.StatusAllegato;
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
                    // Imposta lo stato in base al flag antivirus
                    allegato.setStatusAllegato(
                        s3Service.isAntivirusEnabled()
                            ? StatusAllegato.ANTIVIRUS_ENABLED.name()
                            : StatusAllegato.ANTIVIRUS_DISABLED.name()
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
                })
                .onErrorMap(ex -> new CustomBusinessException(
                    "Errore durante upload/salvataggio allegato: " + ex.getMessage()
                ));
        }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteAllegato(Long allegatoId, String fileKey, boolean isDoc, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione) {
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

            StringBuilder url = new StringBuilder("/allegato/delete/" + allegatoId + "?");
            if (campiModificati != null && !campiModificati.isBlank()) url.append("campiModificati=").append(campiModificati).append("&");
            if (idPiao != null) url.append("idPiao=").append(idPiao).append("&");
            if (codTipologiaFK != null) url.append("codTipologiaFK=").append(codTipologiaFK).append("&");
            if (testoSezione != null && !testoSezione.isBlank()) url.append("testoSezione=").append(testoSezione);
            url.append("&isDoc=").append(isDoc);

            return deleteOperation
                .then(webClientService.delete(url.toString(), webServiceType, headers))
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
    public Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegati(List<String> codTipologia, List<String> codTipologiaAllegato, Long idPiao, boolean isDoc) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String url = "/allegato/by-tipologia?codTipologia=" + String.join(",", codTipologia)
            + "&codTipologiaAllegato=" + String.join(",", codTipologiaAllegato)
            + "&idPiao=" + idPiao
            + "&isDoc=" + isDoc;

        return webClientService.get(
                url,
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
                                .map(presignedUrl -> {
                                    allegato.setDownloadUrl(presignedUrl);
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

    @Override
    public Mono<GenericResponseDTO<List<AllegatoDTO>>> getAllegatiByIdPiao(Long idPiao) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String url = "/allegato/findByIdPiao?idPiao=" + idPiao;

        return webClientService.get(
                url,
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<AllegatoDTO>>() {}
            )
            .map(updatedList -> {
                GenericResponseDTO<List<AllegatoDTO>> response = new GenericResponseDTO<>();
                response.setData(updatedList);
                response.setStatus(Status.builder().isSuccess(true).build());
                return response;
            })
            .onErrorMap(ex -> new CustomBusinessException("Errore nel recupero allegati: " + ex.getMessage()));
    }

    @Override
    public Mono<GenericResponseDTO<AllegatoDTO>> saveAllegatoSenzaUpload(AllegatoDTO allegato) {
        log.info("Salvataggio allegato senza upload S3: codDocumento={}", allegato.getCodDocumento());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

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
            })
            .doOnNext(r -> log.info("Allegato salvato con successo senza upload S3"))
            .onErrorMap(ex -> {
                log.error("Errore salvataggio allegato senza upload S3: {}", ex.getMessage(), ex);
                return new CustomBusinessException("Errore salvataggio allegato: " + ex.getMessage());
            });
    }

    @Override
    public Mono<AllegatoDTO> findAllegatoById(Long id) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return webClientService.get(
                "/allegato/" + id,
                webServiceType,
                headers,
                new ParameterizedTypeReference<AllegatoDTO>() {}
            )
            .doOnNext(a -> log.info("Allegato trovato con id={}", id))
            .onErrorResume(ex -> {
                log.warn("Allegato non trovato o errore per id={}: {}", id, ex.getMessage());
                return Mono.empty();
            });
    }

    @Override
    public Mono<GenericResponseDTO<AllegatoDTO>> updateAllegato(AllegatoDTO allegato) {
        log.info("Update allegato id={}, status={}", allegato.getId(), allegato.getStatusAllegato());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return webClientService.put(
                "/allegato/update",
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
            })
            .doOnNext(r -> log.info("Allegato aggiornato con successo id={}", allegato.getId()))
            .onErrorMap(ex -> {
                log.error("Errore update allegato id={}: {}", allegato.getId(), ex.getMessage(), ex);
                return new CustomBusinessException("Errore update allegato: " + ex.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteBozzaByCodDocumento(String codDocumento) {

        if (codDocumento == null || codDocumento.isBlank() || !codDocumento.startsWith("BOZZA_")) {
            return Mono.error(new CustomBusinessException(
                "codDocumento non valido per delete bozza: " + codDocumento));
        }

        log.info("[SERVICE] Delete bozza PDF: codDocumento={}", codDocumento);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        String url = "/allegato/delete-bozza?codDocumento=" + codDocumento;

        // 1) Elimino prima il file su S3 (codDocumento = key del file su bucket).
        //    Eventuali errori S3 vengono loggati ma NON bloccano il delete su DB
        //    (es. file già rimosso o key non più esistente).
        return s3Service.deleteFile(codDocumento)
            .doOnSuccess(v -> log.info("File S3 eliminato per codDocumento={}", codDocumento))
            .onErrorResume(ex -> {
                log.warn("Errore delete file S3 per codDocumento={}: {} (continuo con delete DB)",
                    codDocumento, ex.getMessage());
                return Mono.empty();
            })
            // 2) Procedo con la cancellazione su DB tramite BE
            .then(webClientService.delete(url, webServiceType, headers))
            .map(res -> {
                GenericResponseDTO<Void> response = new GenericResponseDTO<>();
                response.setStatus(Status.builder().isSuccess(true).build());
                return response;
            })
            .doOnSuccess(r -> log.info("Bozza PDF cancellata con successo: codDocumento={}", codDocumento))
            .onErrorMap(ex -> {
                log.error("Errore delete bozza PDF codDocumento={}: {}", codDocumento, ex.getMessage(), ex);
                return new CustomBusinessException("Errore delete bozza PDF: " + ex.getMessage());
            });
    }
}

