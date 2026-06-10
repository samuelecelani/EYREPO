package it.ey.dto.external;

import it.ey.dto.CampiTecniciDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO unica per lo scambio dati del Ticket.
 * Mappa tutti i campi della tabella "ticket" (schema ServiziComuni).
 * Estende CampiTecniciDTO per ereditare i campi tecnici.
 * Usata sia in ingresso (request) che in uscita (response) per tutte le API.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TicketExternalDTO extends CampiTecniciDTO {

    private Long id;
    private String nome;
    private String cognome;
    private String mail;
    private String oggetto;
    private String messaggio;
    private String idModulo;
    private String codiceFiscale;
    private String codicePa;
    private Long idTicketEsterno;
    private String stato;
    private String messaggioRisposta;

    /** Riferimento alla categoria ticket (solo id, tipologica) */
    private Long idCategoriaTicket;

    /** Allegati provenienti dal sistema di ticketing esterno (struttura diversa da {@link AllegatoTicketDTO}) */
    private List<AllegatoTicketEsternoExternalDTO> allegatiEsterni;

}
