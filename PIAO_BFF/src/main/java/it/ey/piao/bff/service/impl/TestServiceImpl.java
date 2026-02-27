package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.GetTestResponse;
import it.ey.dto.Status;
import it.ey.dto.TestDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.file.GeneratePiao;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ITestService;
import it.ey.piao.bff.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;

@Service
public class TestServiceImpl implements ITestService {

    private final S3Service s3Service;
    private final GeneratePiao generatePdf;
    private final WebClientService webClientService;
    private final WebServiceType  webServiceType;
    private static final Logger log = LoggerFactory.getLogger(TestServiceImpl.class);

    @Autowired
    public TestServiceImpl(S3Service s3Service, GeneratePiao generatePdf, WebClientService webClientService) {
        this.s3Service = s3Service;
        this.generatePdf = generatePdf;
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<Void> Save(TestDTO test) {
        log.info("Avvio salvataggio del test: {}", test);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return webClientService.post("/test", webServiceType,test, headers, TestDTO.class)
            .doOnNext(savedTest -> log.info("Test salvato correttamente: {}", savedTest))
            .flatMap(savedTest -> Mono.fromRunnable(() -> {
                try {
                    log.info("Generazione  e Salvataggio PDF in corso...");
                    log.info("Upload del file su S3...");
                    s3Service.uploadFileGenerated(generatePdf.execute(savedTest));
                    log.info("Upload completato con successo.");
                    log.info("Invio della notifica in corso...");
                } catch (IOException e) {
                    log.error("Errore nella generazione o upload del PDF", e);
                    throw new RuntimeException(e);
                }

            }))
            .onErrorResume(e -> {
                log.error("Errore nella transazione, è richiesto rollback manuale", e);
                return Mono.error(new RuntimeException("Errore nella transazione, rollback manuale richiesto", e));
            })
            .doOnSuccess(v -> log.info("Salvataggio completato"))
            .doOnTerminate(() -> log.info("Fine processo di salvataggio"))
            .then();
    }

    @Override
    public Mono<GenericResponseDTO<TestDTO>> getTestById(Long id) {
        log.info("Richiesta test con ID: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/test/" + id, webServiceType,headers, TestDTO.class)
            .doOnNext(test -> log.info("Test ricevuto: {}", test))
            .map(test -> {
            GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
            finalResponse.setData(test);
            finalResponse.setStatus(new Status());
            finalResponse.getStatus().setSuccess(Boolean.TRUE);
            return finalResponse;
        })
            .doOnError(e -> {
                log.error("Errore nel recupero del test con ID {}", id, e);
                GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<Void> deleteTestById(Long id) {
        log.info("Richiesta cancellazione test con ID: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/test/" + id, webServiceType,headers, Void.class)
            .doOnSuccess(v -> log.info("Test con ID {} cancellato correttamente", id))
            .doOnError(e -> log.error("Errore nella cancellazione del test con ID {}", id, e));
    }



    @Override
    public Mono<GenericResponseDTO<List<TestDTO>>> getAllTest() {
        log.info("Richiesta lista di tutti i test");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");



        return webClientService.get("/test", webServiceType,headers, GetTestResponse.class)
            .doOnNext(response -> log.info("Lista test ricevuta: {}", response))
            .map(test -> {
                GenericResponseDTO<List<TestDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(test.getTests());
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei datu {}", e);
                GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
