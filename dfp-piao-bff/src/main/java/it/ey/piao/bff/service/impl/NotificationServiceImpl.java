package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.INotificationService;
import it.ey.piao.bff.service.NotificationSinkService;
import it.ey.piao.bff.service.S3Service;
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
import java.util.UUID;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final WebClientService webClientService;
    private final NotificationSinkService sinkService;
    private final S3Service s3Service;
    private final WebServiceType notificaBeType;
    private final WebServiceType apiType;
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    public NotificationServiceImpl(WebClientService webClientService,
                                   NotificationSinkService sinkService,
                                   S3Service s3Service) {
        this.webClientService = webClientService;
        this.sinkService = sinkService;
        this.s3Service = s3Service;
        this.notificaBeType = WebServiceType.NOTIFICATION_BE;
        this.apiType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<EmailTaskMessageDTO>> sendEmail(EmailTaskMessageDTO emailTaskMessage) {
        return Mono.defer(() -> {
            // Genera un UUID di riferimento se non presente
            if (emailTaskMessage.getReferenceUuid() == null || emailTaskMessage.getReferenceUuid().isBlank()) {
                emailTaskMessage.setReferenceUuid(UUID.randomUUID().toString());
            }

            log.info("Invio email al Notifica_BE: referenceUuid={}, to={}, oggetto='{}'",
                    emailTaskMessage.getReferenceUuid(),
                    emailTaskMessage.getToAddresses(),
                    emailTaskMessage.getMailObject());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return webClientService
                .post("/api/v1/email/send", notificaBeType, emailTaskMessage, headers,
                    new ParameterizedTypeReference<GenericResponseDTO<EmailTaskMessageDTO>>() {})
                .doOnNext(r -> log.info("Email inviata con successo: referenceUuid={}", emailTaskMessage.getReferenceUuid()))
                .onErrorResume(e -> {
                    log.error("Errore invio email: {}", e.getMessage(), e);
                    GenericResponseDTO<EmailTaskMessageDTO> errorResp = new GenericResponseDTO<>();
                    errorResp.setStatus(Status.builder().isSuccess(false).build());
                    errorResp.setError(new Error());
                    errorResp.getError().setMessageError("Errore invio email: " + e.getMessage());
                    return Mono.just(errorResp);
                });
        });
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
    public Flux<NotificationDTO> getNotifies(String idModulo, String codicePa, String codiceFiscale, boolean userDfp) {
        log.info("Richiesta notifiche storiche per modulo: {}, codicePa: {}, cf: {}, userDfp: {}", idModulo, codicePa, codiceFiscale, userDfp);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        // Costruisce l'URL con i parametri obbligatori e opzionali
        StringBuilder url = new StringBuilder("/api/v1/notification/subscribe?idModulo=")
            .append(idModulo);
        if (codicePa != null && !codicePa.isBlank()) {
            url.append("&codicePa=").append(codicePa);
        }
        if (codiceFiscale != null && !codiceFiscale.isBlank()) {
            url.append("&codiceFiscale=").append(codiceFiscale);
        }
        url.append("&userDfp=").append(userDfp);
        return webClientService.get(url.toString(), notificaBeType, headers,
                new ParameterizedTypeReference<List<NotificationDTO>>() {})
            .flatMapMany(Flux::fromIterable)
            .flatMap(this::enrichWithPresignedUrl)
            .doOnError(e -> log.error("Errore nel recupero notifiche storiche: {}", e.getMessage()));
    }

    /**
     * Se la notifica ha nomeFile valorizzato, genera un presigned URL S3 e lo setta su downloadUrl.
     */
    private Mono<NotificationDTO> enrichWithPresignedUrl(NotificationDTO notification) {
        String nomeFile = notification.getNomeFile();
        if (nomeFile == null || nomeFile.isBlank()) {
            return Mono.just(notification);
        }

        // Prima verifico se l'oggetto esiste su S3 (HEAD), poi genero la presigned URL.
        return s3Service.doesObjectExist(nomeFile)
            .flatMap(exists -> {
                if (Boolean.FALSE.equals(exists)) {
                    log.warn("File non presente su S3 per notifica id={}, nomeFile={}: skip presigned URL",
                        notification.getId(), nomeFile);
                    notification.setDownloadUrl("Non è possibile scaricare il file");
                    return Mono.just(notification);
                }
                return s3Service.generatePresignedUrl(nomeFile)
                    .map(presignedUrl -> {
                        notification.setDownloadUrl(presignedUrl);
                        log.debug("Presigned URL generata per notifica id={}, nomeFile={}", notification.getId(), nomeFile);
                        return notification;
                    });
            })
            .onErrorResume(e -> {
                log.warn("Errore verifica/generazione presigned URL per notifica id={}, nomeFile={}: {}",
                    notification.getId(), nomeFile, e.getMessage());
                return Mono.just(notification);
            });
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
    public Flux<NotificationDTO> subscribeToNotifications(String idModulo, String codicePa, String codiceFiscale) {
        log.info("Nuovo subscriber SSE per modulo '{}', codicePa '{}', cf '{}'", idModulo, codicePa, codiceFiscale);
        return sinkService.getFlux(idModulo, codicePa, codiceFiscale);
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
