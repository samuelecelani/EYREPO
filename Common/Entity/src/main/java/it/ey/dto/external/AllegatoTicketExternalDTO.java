package it.ey.dto.external;

import it.ey.dto.CampiTecniciDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DTO per lo scambio dati dell'Allegato Ticket (proxy verso il modulo ticket-service).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AllegatoTicketExternalDTO extends CampiTecniciDTO {

    private Long id;
    private Long idTicketFk;
    private String codDocumento;
    private String descrizione;
    private String sizeAllegato;
    /** Esito antivirus: NO_THREATS_FOUND | THREATS_FOUND | FAILED | UNSUPPORTED */
    private String esitoAntivirus;
    private String idModulo;
    private String codiceFiscale;
    private String codicePa;
}

