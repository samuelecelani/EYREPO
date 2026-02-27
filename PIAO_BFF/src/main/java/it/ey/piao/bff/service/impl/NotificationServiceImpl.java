package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.INotificationService;
import it.ey.piao.bff.service.NotificationSinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final WebClientService webClientService;
    private final NotificationSinkService sinkService;
    private final WebServiceType notificaBeType;
    private final WebServiceType apiType;
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    public NotificationServiceImpl(WebClientService webClientService, NotificationSinkService sinkService) {
        this.webClientService = webClientService;
        this.sinkService = sinkService;
        this.notificaBeType = WebServiceType.NOTIFICATION_BE;
        this.apiType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<NotificationDTO>> sendNotificationMulticast(NotificationDTO notification) {
        log.info("Delegando invio notifica MULTICAST al Notifica_BE...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return webClientService
            .post("/api/v1/notification/multicast", notificaBeType, notification, headers,
                new ParameterizedTypeReference<GenericResponseDTO<NotificationDTO>>() {})
            .doOnNext(r -> log.info("Notifica MULTICAST inviata con successo via Notifica_BE"))
            .onErrorResume(e -> {
                log.error("Errore invio notifica MULTICAST: {}", e.getMessage(), e);
                return Mono.just(buildErrorResponse(e.getMessage()));
            });
    }

    @Override
    public Mono<GenericResponseDTO<NotificationDTO>> sendNotificationAnyCast(NotificationDTO notification) {
        log.info("Delegando invio notifica ANYCAST al Notifica_BE...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return webClientService
            .post("/api/v1/notification/anycast", notificaBeType, notification, headers,
                new ParameterizedTypeReference<GenericResponseDTO<NotificationDTO>>() {})
            .doOnNext(r -> log.info("Notifica ANYCAST inviata con successo via Notifica_BE"))
            .onErrorResume(e -> {
                log.error("Errore invio notifica ANYCAST: {}", e.getMessage(), e);
                return Mono.just(buildErrorResponse(e.getMessage()));
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<NotificationDTO>>> sendNotificationBatch(List<NotificationDTO> notifications) {
        log.info("Invio batch di {} notifiche al Notifica_BE...", notifications.size());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return webClientService
            .post("/api/v1/notification/multicast/batch", notificaBeType, notifications, headers,
                new ParameterizedTypeReference<GenericResponseDTO<List<NotificationDTO>>>() {})
            .doOnNext(r -> log.info("Batch di {} notifiche inviato con successo", notifications.size()))
            .onErrorResume(e -> {
                log.error("Errore invio batch notifiche: {}", e.getMessage(), e);
                GenericResponseDTO<List<NotificationDTO>> errorResp = new GenericResponseDTO<>();
                errorResp.setStatus(new Status());
                errorResp.getStatus().setSuccess(Boolean.FALSE);
                errorResp.setError(new Error());
                errorResp.getError().setMessageError(e.getMessage());
                return Mono.just(errorResp);
            });
    }

    @Override
    public Mono<GenericResponseDTO<NotificationDTO>> getNotifies(Long id) {
        log.info("Richiesta notifica con ID: {}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/test/" + id, apiType, headers, NotificationDTO.class)
            .map(notification -> {
                GenericResponseDTO<NotificationDTO> response = new GenericResponseDTO<>();
                response.setData(notification);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
                return response;
            });
    }

    @Override
    public Flux<NotificationDTO> getNotifies(String idModulo, String ruolo, String codicePa, String codiceFiscale) {
        log.info("Richiesta notifiche storiche per modulo: {}, ruolo: {}, codicePa: {}, cf: {}", idModulo, ruolo, codicePa, codiceFiscale);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        // Costruisce l'URL con i parametri obbligatori e opzionali
        StringBuilder url = new StringBuilder("/api/v1/notification/subscribe?idModulo=")
            .append(idModulo)
            .append("&ruolo=").append(ruolo)
            .append("&codicePa=").append(codicePa);
        if (codiceFiscale != null && !codiceFiscale.isBlank()) {
            url.append("&codiceFiscale=").append(codiceFiscale);
        }
        return webClientService.get(url.toString(), notificaBeType, headers,
                new ParameterizedTypeReference<List<NotificationDTO>>() {})
            .flatMapMany(Flux::fromIterable)
            .doOnError(e -> log.error("Errore nel recupero notifiche storiche: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> readNotify(NotificationDTO notification) {
        log.info("Segnatura notifica come letta: {}", notification);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return webClientService.put("/api/v1/notification/readNotify", notificaBeType, notification, headers, NotificationDTO.class)
            .then()
            .onErrorResume(e -> {
                log.error("Errore readNotify", e);
                return Mono.error(new RuntimeException("Errore readNotify", e));
            });
    }

    @Override
    public Mono<Void> unreadNotify(NotificationDTO notification) {
        log.info("Segnatura notifica come non letta: {}", notification);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return webClientService.put("/api/v1/notification/unreadNotify", notificaBeType, notification, headers, NotificationDTO.class)
            .then()
            .onErrorResume(e -> {
                log.error("Errore unreadNotify", e);
                return Mono.error(new RuntimeException("Errore unreadNotify", e));
            });
    }

    @Override
    public Flux<NotificationDTO> subscribeToNotifications(String idModulo, String ruolo, String codicePa, String codiceFiscale) {
        log.info("Nuovo subscriber SSE per modulo '{}', ruolo '{}', codicePa '{}', cf '{}'", idModulo, ruolo, codicePa, codiceFiscale);
        return sinkService.getFlux(idModulo, ruolo, codicePa, codiceFiscale);
    }

    // ---- helper ----
    private GenericResponseDTO<NotificationDTO> buildErrorResponse(String message) {
        GenericResponseDTO<NotificationDTO> r = new GenericResponseDTO<>();
        r.setStatus(new Status());
        r.getStatus().setSuccess(Boolean.FALSE);
        r.setError(new Error());
        r.getError().setMessageError(message);
        return r;
    }
}
