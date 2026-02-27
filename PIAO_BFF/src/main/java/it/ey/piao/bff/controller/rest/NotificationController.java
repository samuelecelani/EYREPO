package it.ey.piao.bff.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NotificationDTO;
import it.ey.dto.Status;
import it.ey.enums.TypeNotification;
import it.ey.piao.bff.service.INotificationService;
import it.ey.piao.bff.service.NotificationSinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApiV1Controller("/notification")
class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final INotificationService notificationService;
    private final NotificationSinkService sinkService;

    @Autowired
    public NotificationController(INotificationService notificationService,
                                  NotificationSinkService sinkService) {
        this.notificationService = notificationService;
        this.sinkService = sinkService;
    }

    @PostMapping("/sendMail")
    @Operation(summary = "Invio email", description = "Invia una notifica di tipo EMAIL sul topic multicast")
    public Mono<ResponseEntity<GenericResponseDTO<NotificationDTO>>> sendMail(
            @RequestBody NotificationDTO request) {
        return notificationService.sendNotificationMulticast(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Errore sendMail: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    @PostMapping("/sendNotify")
    @Operation(summary = "Invio notifica multicast", description = "Pubblica una notifica sul Topic ActiveMQ")
    public Mono<ResponseEntity<GenericResponseDTO<NotificationDTO>>> sendNotify(
            @RequestBody NotificationDTO request) {
        return notificationService.sendNotificationMulticast(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Errore sendNotify: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    @PostMapping("/anycast")
    @Operation(summary = "Invio notifica anycast", description = "Pubblica una notifica sulla Queue ActiveMQ (point-to-point)")
    public Mono<ResponseEntity<GenericResponseDTO<NotificationDTO>>> sendAnycast(
            @RequestBody NotificationDTO request) {
        return notificationService.sendNotificationAnyCast(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Errore sendAnycast: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    @PostMapping("/sendBatch")
    @Operation(summary = "Invio batch di notifiche",
               description = "Riceve una lista di notifiche e le invia al Notifica_BE in un'unica chiamata. " +
                             "Il Notifica_BE le scrive sulla coda ActiveMQ una per una. Il WORKER le scoda e le salva su DB man mano.")
    public Mono<ResponseEntity<GenericResponseDTO<List<NotificationDTO>>>> sendBatch(
            @RequestBody List<NotificationDTO> notifications) {
        log.info("Ricevuta richiesta batch di {} notifiche", notifications.size());
        return notificationService.sendNotificationBatch(notifications)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Errore sendBatch: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    @PutMapping("/readNotify")
    @Operation(summary = "Segna notifica come letta")
    public Mono<ResponseEntity<String>> readNotify(@RequestBody NotificationDTO request) {
        return notificationService.readNotify(request)
            .thenReturn(ResponseEntity.ok("OK"))
            .onErrorResume(e -> {
                log.error("Errore readNotify: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore"));
            });
    }

    @PutMapping("/unreadNotify")
    @Operation(summary = "Segna notifica come non letta")
    public Mono<ResponseEntity<String>> unreadNotify(@RequestBody NotificationDTO request) {
        return notificationService.unreadNotify(request)
            .thenReturn(ResponseEntity.ok("OK"))
            .onErrorResume(e -> {
                log.error("Errore unreadNotify: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore"));
            });
    }

    @GetMapping("/list")
    @Operation(summary = "Lista notifiche storiche", description = "Recupera le notifiche salvate su DB per modulo, ruolo, codicePa e opzionalmente codice fiscale")
    public Mono<ResponseEntity<GenericResponseDTO<Object>>> getNotificationList(
            @RequestParam("idModulo") String idModulo,
            @RequestParam("ruolo") String ruolo,
            @RequestParam("codicePa") String codicePa,
            @RequestParam(value = "codiceFiscale", required = false) String codiceFiscale,
            @RequestParam(value = "numeroPagina", defaultValue = "1") int numeroPagina,
            @RequestParam(value = "righePerPagina", defaultValue = "20") int righePerPagina) {
        return notificationService.getNotifies(idModulo, ruolo, codicePa, codiceFiscale)
            .collectList()
            .map(list -> {
                // Wrappa la lista nel formato atteso dal FE: { risultati, totalePagine, totaleElementi }
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("risultati", list);
                body.put("totaleElementi", list.size());
                body.put("totalePagine", (int) Math.ceil((double) list.size() / righePerPagina));
                GenericResponseDTO<Object> response = new GenericResponseDTO<>();
                response.setData(body);
                response.setStatus(new Status());
                response.getStatus().setSuccess(true);
                return ResponseEntity.ok(response);
            })
            .onErrorResume(e -> {
                log.error("Errore getNotificationList: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    /**
     * SSE real-time: il FE si connette una sola volta e riceve le notifiche in push.
     * ⚠️ SWAGGER NON SUPPORTA SSE: usare curl o il FE per testare questo endpoint.
     * curl: curl -N "http://localhost:9082/api/v1/notification/subscribe?idModulo=PIAO"
     */




    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<NotificationDTO>> subscribe(
            @RequestParam("idModulo") String idModulo,
            @RequestParam("ruolo") String ruolo,
            @RequestParam("codicePa") String codicePa,
            @RequestParam(value = "codiceFiscale", required = false) String codiceFiscale) {
        log.info(">>> Nuovo subscriber SSE per modulo '{}', ruolo '{}', codicePa '{}', cf '{}'", idModulo, ruolo, codicePa, codiceFiscale);

        // Heartbeat ogni 30s: evita che browser/proxy chiudano la connessione idle
        Flux<ServerSentEvent<NotificationDTO>> heartbeat = Flux.interval(Duration.ofSeconds(30))
            .map(i -> ServerSentEvent.<NotificationDTO>builder()
                .comment("heartbeat")
                .build());

        Flux<ServerSentEvent<NotificationDTO>> notifications = notificationService
            .subscribeToNotifications(idModulo, ruolo, codicePa, codiceFiscale)
            .map(notification -> ServerSentEvent.<NotificationDTO>builder()
                .id(String.valueOf(System.currentTimeMillis()))
                .event("notification")
                .data(notification)
                .build());

        return Flux.merge(heartbeat, notifications)
            .doOnSubscribe(s -> log.info(">>> SSE stream aperto per modulo '{}', ruolo '{}', codicePa '{}', cf '{}'", idModulo, ruolo, codicePa, codiceFiscale))
            .doOnCancel(() -> log.info(">>> Client SSE disconnesso per modulo '{}', ruolo '{}', codicePa '{}', cf '{}'", idModulo, ruolo, codicePa, codiceFiscale))
            .onErrorResume(e -> {
                log.error("Errore SSE per modulo '{}': {}", idModulo, e.getMessage());
                return Flux.empty();
            });
    }

    /**
     * Endpoint di test: emette direttamente una notifica nel Sink per verificare
     * che il canale SSE funzioni, senza passare da ActiveMQ.
     * Usare PRIMA di /subscribe, poi lanciare questo endpoint.
     */
    @PostMapping("/test-emit")
    @Operation(summary = "TEST: emit diretto nel Sink SSE", description = "Solo per test: pubblica direttamente nel Sink senza passare da ActiveMQ")
    public Mono<ResponseEntity<String>> testEmit(@RequestParam("idModulo") String idModulo) {
        NotificationDTO test = new NotificationDTO();
        test.setIdModulo(idModulo);
        test.setMessage("Test SSE diretto");
        test.setType(TypeNotification.NOTIFICATION_WEB);
        sinkService.emit(test);
        return Mono.just(ResponseEntity.ok("Emit eseguito per modulo: " + idModulo));
    }
}
