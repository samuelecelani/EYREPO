package it.ey.notifica.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import it.ey.notifica.annotation.ApiV1Controller;
import it.ey.notifica.dto.GenericResponseDTO;
import it.ey.notifica.dto.NotificationDTO;
import it.ey.notifica.service.INotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * Controller reattivo (WebFlux) per l'invio di notifiche su code ActiveMQ
 * e per la lettura/gestione delle notifiche da DB.
 * Espone endpoint richiamabili dal BFF o da altri moduli.
 * - POST /api/v1/notification/multicast  → scrive sul Topic (MULTICAST)
 * - POST /api/v1/notification/anycast    → scrive sulla Queue (ANYCAST)
 * - GET  /api/v1/notification/subscribe  → recupera notifiche da DB per idModulo
 * - PUT  /api/v1/notification/readNotify → segna notifica come letta
 * - PUT  /api/v1/notification/unreadNotify → segna notifica come non letta
 */
@ApiV1Controller("/notification")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final INotificationService notificationService;

    @Autowired
    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Invia una notifica in modalità MULTICAST sul Topic ActiveMQ.
     */
    @PostMapping("/multicast")
    @Operation(summary = "Invio notifica MULTICAST", description = "Pubblica una notifica sul Topic ActiveMQ (pub/sub)")
    public Mono<ResponseEntity<GenericResponseDTO<NotificationDTO>>> sendMulticast(
            @RequestBody NotificationDTO request) {
        log.info("Ricevuta richiesta MULTICAST per modulo: {}", request.getIdModulo());
        return notificationService.sendMulticast(request)
                .map(result -> ResponseEntity.ok(result))
                .onErrorResume(e -> {
                    log.error("Errore controller MULTICAST: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Invia una notifica in modalità ANYCAST sulla Queue ActiveMQ.
     */
    @PostMapping("/anycast")
    @Operation(summary = "Invio notifica ANYCAST", description = "Pubblica una notifica sulla Queue ActiveMQ (point-to-point)")
    public Mono<ResponseEntity<GenericResponseDTO<NotificationDTO>>> sendAnycast(
            @RequestBody NotificationDTO request) {
        log.info("Ricevuta richiesta ANYCAST per modulo: {}", request.getIdModulo());
        return notificationService.sendAnycast(request)
                .map(result -> ResponseEntity.ok(result))
                .onErrorResume(e -> {
                    log.error("Errore controller ANYCAST: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Invia una lista di notifiche in modalità MULTICAST sul Topic ActiveMQ.
     * Itera internamente e scrive ogni notifica sulla coda una per una.
     */
    @PostMapping("/multicast/batch")
    @Operation(summary = "Invio batch notifiche MULTICAST",
               description = "Riceve una lista di notifiche e le scrive sulla coda ActiveMQ una per una. Il WORKER le scoda e le salva su DB man mano.")
    public Mono<ResponseEntity<GenericResponseDTO<List<NotificationDTO>>>> sendMulticastBatch(
            @RequestBody List<NotificationDTO> notifications) {
        log.info("Ricevuta richiesta MULTICAST BATCH di {} notifiche", notifications.size());
        return notificationService.sendMulticastBatch(notifications)
                .map(result -> ResponseEntity.ok(result))
                .onErrorResume(e -> {
                    log.error("Errore controller MULTICAST BATCH: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Recupera tutte le notifiche per un dato idModulo, ruolo, codicePa e opzionalmente codiceFiscale dal DB.
     */
    @GetMapping("/subscribe")
    @Operation(summary = "Recupera notifiche", description = "Recupera la lista di notifiche per idModulo, ruolo, codicePa e opzionalmente codiceFiscale dal database")
    public Mono<ResponseEntity<List<NotificationDTO>>> getNotifications(
            @RequestParam("idModulo") String idModulo,
            @RequestParam("ruolo") String ruolo,
            @RequestParam("codicePa") String codicePa,
            @RequestParam(value = "codiceFiscale", required = false) String codiceFiscale) {
        log.info("Ricevuta richiesta GET notifiche per modulo: {}, ruolo: {}, codicePa: {}, cf: {}", idModulo, ruolo, codicePa, codiceFiscale);
        return Mono.fromCallable(() -> notificationService.getNotifications(idModulo, ruolo, codicePa, codiceFiscale))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore recupero notifiche per modulo {}: {}", idModulo, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Segna una notifica come letta.
     */
    @PutMapping("/readNotify")
    @Operation(summary = "Segna notifica come letta", description = "Aggiorna lo stato della notifica a letta")
    public Mono<ResponseEntity<Void>> readNotification(@RequestBody NotificationDTO notificationDTO) {
        log.info("Ricevuta richiesta readNotify per id: {}", notificationDTO.getId());
        return Mono.fromRunnable(() -> notificationService.readNotifications(notificationDTO))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Errore readNotify: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Segna una notifica come non letta.
     */
    @PutMapping("/unreadNotify")
    @Operation(summary = "Segna notifica come non letta", description = "Aggiorna lo stato della notifica a non letta")
    public Mono<ResponseEntity<Void>> unreadNotification(@RequestBody NotificationDTO notificationDTO) {
        log.info("Ricevuta richiesta unreadNotify per id: {}", notificationDTO.getId());
        return Mono.fromRunnable(() -> notificationService.unreadNotification(notificationDTO))
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Errore unreadNotify: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
