package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {
    private final NotificationProducer notificationProducer;
    private final WebClientService webClientService;
    private final WebServiceType  webServiceType;
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    @Autowired
    public NotificationServiceImpl(NotificationProducer notificationProducer, WebClientService webClientService) {
        this.notificationProducer = notificationProducer;
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public GenericResponseDTO<NotificationDTO> sendNotificationMulticast(NotificationDTO notification) {
    GenericResponseDTO<NotificationDTO> responseDTO = new GenericResponseDTO<>();
    try {
        log.info("Invio notifica mulitcast...");
        //Scrive sulla coda il messaggio
        notificationProducer.sendMulticast(notification);
        log.info("Invio notifica mulitcast avventuo con successo");
        log.info("Propago la request per il FE");
        responseDTO.setData(notification);
        responseDTO.setStatus(new Status());
        responseDTO.getStatus().setSuccess(Boolean.TRUE);
    }catch (Exception e){
        log.error("Errore invio notifica {}" ,notification.getMessage() );
        responseDTO.setStatus(new Status());
        responseDTO.getStatus().setSuccess(Boolean.FALSE);
        responseDTO.setError(new Error());
        responseDTO.getError().setMessageError(e.getMessage());
    }

        return responseDTO;
    }

    @Override
    public GenericResponseDTO<NotificationDTO> sendNotificationAnyCast(NotificationDTO notification) {
        GenericResponseDTO<NotificationDTO> responseDTO = new GenericResponseDTO<>();
        try {
            log.info("Invio notifica anycast...");
            notificationProducer.sendAnycast(notification);
            log.info("Invio notifica anycast avventuo con successo");
            log.info("Propago la request per il FE");
            responseDTO.setData(notification);
            responseDTO.setStatus(new Status());
            responseDTO.getStatus().setSuccess(Boolean.TRUE);
        }catch (Exception e){
            log.error("Errore invio notifica {}" ,notification.getMessage() );
            responseDTO.setStatus(new Status());
            responseDTO.getStatus().setSuccess(Boolean.FALSE);
            responseDTO.setError(new Error());
            responseDTO.getError().setErrorCode(e.getMessage());
        }

        return responseDTO;
    }
    @Override
    public Mono<GenericResponseDTO<NotificationDTO>> getNotifies(Long id) {
        log.info("Richiesta test con ID: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/test/" + id, webServiceType,headers, NotificationDTO.class)
            .doOnNext(notification -> log.info("Test ricevuto: {}", notification))
            .map(notification -> {

                GenericResponseDTO<NotificationDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(notification);
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
    public Flux<NotificationDTO> getNotifies(String idModulo) {
        log.info("Richiesta lista di tutti i test");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
log.info(webServiceType.getUrl() + "/notification/subscribe?idModulo=" + idModulo);
        return webClientService.get("/notification/subscribe?idModulo=" + idModulo , webServiceType, headers,
                new ParameterizedTypeReference<List<NotificationDTO>>() {})
            .doOnNext(response -> log.info("Lista test ricevuta: {}", response))
            .flatMapMany(Flux::fromIterable) // 🔁 converte la lista in un flusso
            .doOnError(e -> log.error("Errore nel recupero dei dati {}", e));
    }



    @Override
    public Mono<Void> readNotify(NotificationDTO notification) {
        log.info("Avvio salvataggio del test: {}", notification);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return webClientService.put("/notification/readNotify", webServiceType,notification, headers, NotificationDTO.class)
            .doOnNext(savedTest -> log.info("Test salvato correttamente: {}", savedTest))
            .flatMap(savedTest -> Mono.fromRunnable(() -> {
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
    public Mono<Void> unreadNotify(NotificationDTO notification) {
        log.info("Avvio salvataggio del test: {}", notification);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        return webClientService.put("/notification/unreadNotify", webServiceType,notification, headers, NotificationDTO.class)
            .doOnNext(savedTest -> log.info("Test salvato correttamente: {}", savedTest))
            .flatMap(savedTest -> Mono.fromRunnable(() -> {
            }))
            .onErrorResume(e -> {
                log.error("Errore nella transazione, è richiesto rollback manuale", e);
                return Mono.error(new RuntimeException("Errore nella transazione, rollback manuale richiesto", e));
            })
            .doOnSuccess(v -> log.info("Salvataggio completato"))
            .doOnTerminate(() -> log.info("Fine processo di salvataggio"))
            .then();
    }
}
