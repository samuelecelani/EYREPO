package it.ey.piao.bff.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.external.AllegatoTicketExternalDTO;
import it.ey.dto.external.CategoriaTicketExternalDTO;
import it.ey.dto.external.TicketExternalDTO;
import it.ey.piao.bff.service.ITicketService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controller di proxy verso il modulo TICKET_BE (dfp-ticket-service).
 * Replica tutti gli endpoint esposti dal modulo ticket; la logica sta tutta nel modulo esterno.
 */
@ApiV1Controller("/ticket")
@Tag(name = "Ticket", description = "API di proxy per la gestione dei ticket e allegati")
class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);
    private final ITicketService ticketService;

    @Autowired
    public TicketController(ITicketService ticketService) {
        this.ticketService = ticketService;
    }

    // ======================== TICKET ========================

    @PostMapping
    @Operation(summary = "Apri un nuovo ticket",
               description = "Proxy verso TICKET_BE: crea un ticket e lo invia al sistema di Ticketing esterno.")
    public Mono<ResponseEntity<GenericResponseDTO<TicketExternalDTO>>> apriTicket(
            @Valid @RequestBody TicketExternalDTO ticketDTO) {
        log.info("Ricevuta richiesta apertura ticket - oggetto: {}", ticketDTO.getOggetto());
        return ticketService.apriTicket(ticketDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller apriTicket: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/{id}")
    @Operation(summary = "Aggiorna ticket",
               description = "Proxy verso TICKET_BE: aggiorna i dati del ticket.")
    public Mono<ResponseEntity<GenericResponseDTO<TicketExternalDTO>>> aggiornaTicket(
            @PathVariable("id") Long ticketId,
            @Valid @RequestBody TicketExternalDTO ticketDTO) {
        log.info("Ricevuta richiesta aggiornamento ticket id: {}", ticketId);
        return ticketService.aggiornaTicket(ticketId, ticketDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller aggiornaTicket id {}: {}", ticketId, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/helpdesk/close")
    @Operation(summary = "Chiudi ticket da HelpDesk",
               description = "Proxy verso TICKET_BE: notifica la chiusura di un ticket dal sistema HelpDesk.")
    public Mono<ResponseEntity<GenericResponseDTO<TicketExternalDTO>>> chiudiTicketDaHelpdesk(
            @Valid @RequestBody TicketExternalDTO ticketDTO) {
        log.info("Ricevuta richiesta chiusura ticket da HelpDesk - idTicketEsterno: {}", ticketDTO.getIdTicketEsterno());
        return ticketService.chiudiTicketDaHelpdesk(ticketDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller chiudiTicketDaHelpdesk: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // ======================== ALLEGATI ========================

    @PostMapping(value = "/allegato", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Aggiungi allegato",
               description = "Proxy verso TICKET_BE: inoltra il multipart (allegato JSON + file) al modulo ticket.")
    public Mono<ResponseEntity<GenericResponseDTO<AllegatoTicketExternalDTO>>> aggiungiAllegato(
            @RequestPart("allegato") Mono<AllegatoTicketExternalDTO> allegatoMono,
            @RequestPart("file") Mono<FilePart> filePartMono) {
        return Mono.zip(allegatoMono, filePartMono)
                .flatMap(tuple -> {
                    AllegatoTicketExternalDTO allegato = tuple.getT1();
                    FilePart filePart = tuple.getT2();
                    log.info("[POST /ticket/allegato] Inserimento allegato per ticket id: {} - file: {}",
                            allegato.getIdTicketFk(), filePart.filename());
                    return ticketService.aggiungiAllegato(allegato, filePart);
                })
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller aggiungiAllegato: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/{id}/allegati")
    @Operation(summary = "Recupera allegati di un ticket",
               description = "Proxy verso TICKET_BE: restituisce la lista degli allegati associati al ticket.")
    public Mono<ResponseEntity<GenericResponseDTO<List<AllegatoTicketExternalDTO>>>> getAllegatiByTicket(
            @PathVariable("id") Long idTicket) {
        log.info("Ricevuta richiesta recupero allegati per ticket id: {}", idTicket);
        return ticketService.getAllegatiByTicket(idTicket)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller getAllegatiByTicket id {}: {}", idTicket, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @PutMapping("/allegato/{id}/antivirus")
    @Operation(summary = "Aggiorna esito antivirus allegato",
               description = "Proxy verso TICKET_BE: aggiorna il campo esitoAntivirus sull'allegato.")
    public Mono<ResponseEntity<GenericResponseDTO<AllegatoTicketExternalDTO>>> aggiornaEsitoAntivirus(
            @PathVariable("id") Long allegatoId,
            @Valid @RequestBody AllegatoTicketExternalDTO allegatoTicketDTO) {
        log.info("Ricevuta richiesta aggiornamento esito antivirus per allegato id: {}", allegatoId);
        return ticketService.aggiornaEsitoAntivirus(allegatoId, allegatoTicketDTO)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller aggiornaEsitoAntivirus id {}: {}", allegatoId, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @DeleteMapping("/allegato/{id}")
    @Operation(summary = "Elimina allegato",
               description = "Proxy verso TICKET_BE: cancella l'allegato sia da S3 sia dalla tabella allegato_ticket.")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> eliminaAllegato(
            @PathVariable("id") Long allegatoId) {
        log.info("Ricevuta richiesta eliminazione allegato id: {}", allegatoId);
        return ticketService.eliminaAllegato(allegatoId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller eliminaAllegato id {}: {}", allegatoId, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    // ======================== CATEGORIE ========================

    @GetMapping("/categorie")
    @Operation(summary = "Recupera tutte le categorie ticket",
               description = "Proxy verso TICKET_BE: restituisce la lista di tutte le categorie ticket.")
    public Mono<ResponseEntity<GenericResponseDTO<List<CategoriaTicketExternalDTO>>>> getAllCategorie() {
        log.info("Ricevuta richiesta recupero tutte le categorie ticket");
        return ticketService.getAllCategorie()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller getAllCategorie: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    @GetMapping("/categorie/{idModulo}")
    @Operation(summary = "Recupera categorie ticket per modulo",
               description = "Proxy verso TICKET_BE: restituisce le categorie ticket filtrate per id_modulo.")
    public Mono<ResponseEntity<GenericResponseDTO<List<CategoriaTicketExternalDTO>>>> getCategorieByModulo(
            @PathVariable("idModulo") String idModulo) {
        log.info("Ricevuta richiesta recupero categorie per modulo: {}", idModulo);
        return ticketService.getCategorieByModulo(idModulo)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Errore controller getCategorieByModulo {}: {}", idModulo, e.getMessage(), e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}

