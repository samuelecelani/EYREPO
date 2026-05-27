package it.ey.dto.external;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DTO interna che rappresenta un allegato proveniente dal sistema di ticketing esterno.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AllegatoTicketEsternoExternalDTO {

    /** Identificativo dell'allegato nel sistema esterno */
    private Long id;

    /** Nome del file allegato */
    private String filename;
}
