package it.ey.piao.bff.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import it.ey.dto.*;
import it.ey.dto.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.ey.piao.bff.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import  it.ey.utils.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Rest controller usando i flussi reattivi

@RestController
@RequestMapping("/notification")
class NotificationController {

private final INotificationService notificationService;
private final static Logger log = LoggerFactory.getLogger(NotificationController.class);
   @Autowired
   public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @PostMapping("/sendMail")
    @Operation(summary = "Invio email", description = "Invio email")
    public ResponseEntity<GenericResponseDTO<NotificationDTO>> sendNotification (@RequestBody NotificationDTO request){
       try {
           EmailMessageDTO emailMessageDTO = WorkerUtil.getObject(request.getMessage(), EmailMessageDTO.class);
           return ResponseEntity
               .status(HttpStatus.OK)
               .body(notificationService.sendNotificationMulticast(request));
       }catch (Exception e) {
           GenericResponseDTO<NotificationDTO> response = new GenericResponseDTO<>();
           response.setError(new Error());
           response.getError().setMessageError("Oggetto input non formattato correttamente (EMAIL)");
           return ResponseEntity
               .status(HttpStatus.BAD_REQUEST)
               .body(response);
       }
    }
    @PostMapping("/sendNotify")
    @Operation(summary = "Invio notifica", description = "Invio notifica")
    public ResponseEntity<GenericResponseDTO<NotificationDTO>> sendNotify (@RequestBody NotificationDTO request){
        return new ResponseEntity<>(notificationService.sendNotificationMulticast(request), HttpStatus.OK);
    }

    @PostMapping("/anycast")
    @Operation(summary = "Invio ", description = "Invio ")
    public ResponseEntity<GenericResponseDTO<NotificationDTO>> sendNotificationAnyCast (@RequestBody NotificationDTO request){
        return new ResponseEntity<>(notificationService.sendNotificationAnyCast(request), HttpStatus.OK);
    }

    @PutMapping("/readNotify")
    @Operation(summary = "Lettura di una notifica", description = "Permette di segnare come letta una notifica")
    public Mono<ResponseEntity<String>> readNotify(@RequestBody NotificationDTO request) {
        return notificationService.readNotify(request)
            .thenReturn(ResponseEntity.ok("OK"))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore")));
    }
    @PutMapping("/unreadNotify")
    @Operation(summary = "Lettura di una notifica", description = "Permette di segnare come letta una notifica")
    public Mono<ResponseEntity<String>> unreadNotify(@RequestBody NotificationDTO request) {
        return notificationService.readNotify(request)
            .thenReturn(ResponseEntity.ok("OK"))
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore")));
    }
    //Questo servizio permette la gestione real-time delle notifiche.
    // Affinchè funzioni è necessario che il FE chiama in pooling questo servizio utilizzando il protocollo SSE
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<NotificationDTO>> subscribe(@RequestParam("idModulo") String idModulo) {
        return notificationService.getNotifies(idModulo)
            .map(notification -> ServerSentEvent.builder(notification).build())
            .doOnCancel(() -> log.info("Client SSE disconnesso per modulo {}", idModulo))
            .onErrorResume(e -> Flux.empty());
    }




}
