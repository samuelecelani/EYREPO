package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.external.AllegatoTicketExternalDTO;
import it.ey.dto.external.CategoriaTicketExternalDTO;
import it.ey.dto.external.TicketExternalDTO;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service di puro proxy verso il modulo TICKET_BE (dfp-ticket-service).
 * Ogni metodo reindirizza la chiamata al modulo esterno senza logica di business.
 */
public interface ITicketService {

    // ======================== TICKET ========================

    Mono<GenericResponseDTO<TicketExternalDTO>> apriTicket(TicketExternalDTO ticketDTO);

    Mono<GenericResponseDTO<TicketExternalDTO>> aggiornaTicket(Long ticketId, TicketExternalDTO ticketDTO);

    Mono<GenericResponseDTO<TicketExternalDTO>> chiudiTicketDaHelpdesk(TicketExternalDTO ticketDTO);

    // ======================== ALLEGATI ========================

    Mono<GenericResponseDTO<AllegatoTicketExternalDTO>> aggiungiAllegato(AllegatoTicketExternalDTO allegatoTicketDTO,
                                                                        FilePart filePart);

    Mono<GenericResponseDTO<List<AllegatoTicketExternalDTO>>> getAllegatiByTicket(Long idTicket);

    Mono<GenericResponseDTO<AllegatoTicketExternalDTO>> aggiornaEsitoAntivirus(Long allegatoId,
                                                                              AllegatoTicketExternalDTO allegatoTicketDTO);

    Mono<GenericResponseDTO<Void>> eliminaAllegato(Long allegatoId);

    // ======================== CATEGORIE ========================

    Mono<GenericResponseDTO<List<CategoriaTicketExternalDTO>>> getAllCategorie();

    Mono<GenericResponseDTO<List<CategoriaTicketExternalDTO>>> getCategorieByModulo(String idModulo);
}

