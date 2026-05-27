package it.ey.piao.bff.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EmailTaskMessageDTO;
import it.ey.dto.ExcelNotificationDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NotificationDTO;
import it.ey.dto.PdfNotificationDTO;
import it.ey.dto.Status;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.TypeNotification;
import it.ey.piao.bff.service.IExcelGenerationService;
import it.ey.piao.bff.service.INotificationService;
import it.ey.piao.bff.service.IPdfGenerationService;
import it.ey.piao.bff.service.NotificationSinkService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApiV1Controller("/notification")
class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final INotificationService notificationService;
    private final NotificationSinkService sinkService;
    private final IPdfGenerationService pdfGenerationService;
    private final IExcelGenerationService excelGenerationService;
    private final EmailNotificationHelper emailNotificationHelper;

    @Value("${notification.sse.heartbeat-interval-seconds}")
    private int heartbeatIntervalSeconds;

    @Autowired
    public NotificationController(INotificationService notificationService,
                                  NotificationSinkService sinkService,
                                  IPdfGenerationService pdfGenerationService,
                                  IExcelGenerationService excelGenerationService,
                                  EmailNotificationHelper emailNotificationHelper) {
        this.notificationService = notificationService;
        this.sinkService = sinkService;
        this.pdfGenerationService = pdfGenerationService;
        this.excelGenerationService = excelGenerationService;
        this.emailNotificationHelper = emailNotificationHelper;
    }

    @PostMapping("/sendMail")
    @Operation(summary = "Invio email", description = "Invia un'email accodandola sulla coda dfp-email tramite il Notifica_BE")
    public Mono<ResponseEntity<GenericResponseDTO<EmailTaskMessageDTO>>> sendMail(
            @RequestBody EmailTaskMessageDTO request) {
        return notificationService.sendEmail(request)
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
    @Operation(summary = "Lista notifiche storiche", description = "Recupera le notifiche salvate su DB per modulo, codicePa e opzionalmente codice fiscale")
    public Mono<ResponseEntity<GenericResponseDTO<Object>>> getNotificationList(
            @RequestParam("idModulo") String idModulo,
            @RequestParam(value = "codicePa", required = false) String codicePa,
            @RequestParam(value = "codiceFiscale", required = false) String codiceFiscale,
            @RequestParam(value = "userDfp", defaultValue = "false") boolean userDfp,
            @RequestParam(value = "numeroPagina", defaultValue = "1") int numeroPagina,
            @RequestParam(value = "righePerPagina", defaultValue = "20") int righePerPagina) {
        return notificationService.getNotifies(idModulo, codicePa, codiceFiscale, userDfp)
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
            @RequestParam(value = "codicePa", required = false) String codicePa,
            @RequestParam(value = "codiceFiscale", required = false) String codiceFiscale) {
        log.info(">>> Nuovo subscriber SSE per modulo '{}', codicePa '{}', cf '{}'", idModulo, codicePa, codiceFiscale);

        // Heartbeat configurabile: evita che HAProxy/OpenShift chiudano la connessione idle (timeout 504)
        Flux<ServerSentEvent<NotificationDTO>> heartbeat = Flux.interval(Duration.ofSeconds(heartbeatIntervalSeconds))
            .map(i -> ServerSentEvent.<NotificationDTO>builder()
                .comment("heartbeat")
                .build());

        Flux<ServerSentEvent<NotificationDTO>> notifications = notificationService
            .subscribeToNotifications(idModulo, codicePa, codiceFiscale)
            .map(notification -> ServerSentEvent.<NotificationDTO>builder()
                .id(String.valueOf(System.currentTimeMillis()))
                .event("notification")
                .data(notification)
                .build());

        return Flux.merge(heartbeat, notifications)
            .doOnSubscribe(s -> log.info(">>> SSE stream aperto per modulo '{}', codicePa '{}', cf '{}'", idModulo, codicePa, codiceFiscale))
            .doOnCancel(() -> log.info(">>> Client SSE disconnesso per modulo '{}', codicePa '{}', cf '{}'", idModulo, codicePa, codiceFiscale))
            .onErrorResume(e -> {
                // Disconnessione client (chiusura tab/navigazione): log a livello DEBUG, non ERROR
                if (e instanceof IOException || e instanceof ClosedChannelException || isClientDisconnect(e)) {
                    log.debug("Client SSE disconnesso (IO) per modulo '{}': {}", idModulo, e.getMessage());
                } else {
                    log.error("Errore SSE per modulo '{}': {}", idModulo, e.getMessage());
                }
                return Flux.empty();
            });
    }

    /**
     * Endpoint di test: emette direttamente una notifica nel Sink per verificare
     * che il canale SSE funzioni, senza passare da ActiveMQ.
     * Usare PRIMA di /subscribe, poi lanciare questo endpoint.
     */
    /**
     * Verifica se l'eccezione è causata dalla disconnessione del client.
     */
    private boolean isClientDisconnect(Throwable ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof IOException || cause instanceof ClosedChannelException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @PostMapping("/pdf/generation")
    @Operation(summary = "Genera PDF PIAO/Sezione",
               description = "Costruisce un PdfNotificationDTO con i dati del PIAO e delle sezioni, " +
                             "e lo invia al Notifica_BE per la generazione del PDF. " +
                             "Se sezione=PIAO recupera tutte le sezioni, altrimenti solo quella specificata.")
    public Mono<ResponseEntity<GenericResponseDTO<PdfNotificationDTO>>> generatePdf(
            @RequestParam("idPiao") Long idPiao,
            @RequestParam("sezione") Sezione sezione,
            @RequestParam("codicePa") String codicePa) {
        log.info("Richiesta generazione PDF per idPiao={}, sezione={}, codicePa={}", idPiao, sezione, codicePa);
        return pdfGenerationService.generatePdf(idPiao, sezione, codicePa)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Errore generatePdf: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    @PostMapping("/excel/generation/batch")
    @Operation(summary = "Genera Excel PIAO batch",
               description = "Riceve una lista di idPiao, recupera i dati PiaoExternal dal BE per ciascuno, " +
                             "costruisce ExcelNotificationDTO e li invia al Notifica_BE per la scrittura sulla coda.")
    public Mono<ResponseEntity<GenericResponseDTO<List<ExcelNotificationDTO>>>> generateExcelBatch(
            @RequestBody List<Long> idPiaoList,
            @RequestParam(value = "codicePa", required = false) String codicePa) {
        log.info("Richiesta generazione Excel batch per idPiaoList={}, codicePa={}", idPiaoList, codicePa);
        return excelGenerationService.generateExcelBatch(idPiaoList, codicePa)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Errore generateExcelBatch: {}", e.getMessage(), e);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

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

    /**
     * Invia email ai referenti delle PA specificate.
     * Accetta una lista di codici PA e una chiave di configurazione per MAIL_OBJECT e BODY_EMAIL.
     *
     * @param codiciPa lista di codici PA destinatari
     * @param key      chiave di configurazione per recuperare oggetto e body email
     * @return Mono<Void> che completa dopo l'invio di tutte le email
     */
    @GetMapping("/send-email-referenti")
    @Operation(summary = "Invio email ai referenti",
               description = "Invia email ai referenti delle PA indicate, usando la chiave di configurazione specificata per oggetto e body")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> sendEmailToReferenti(
            @RequestParam("codiciPa") List<String> codiciPa,
            @RequestParam("key") String key) {
        log.info("Invio email ai referenti per codiciPa={}, key={}", codiciPa, key);

        return Flux.fromIterable(codiciPa)
            .flatMap(codicePa -> emailNotificationHelper.sendEmailToUtentiByRuoli(
                codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name()),
                null,
                null,
                key))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> response = new GenericResponseDTO<>();
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(ResponseEntity.ok(response));
            }))
            .onErrorResume(e -> {
                log.error("Errore invio email ai referenti: {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
            });
    }
}
