package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Estende PropertyDTO aggiungendo campi specifici per le attività:
 * - keyDateInizio / valueDateInizio
 * - keyDateFine  / valueDateFine
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PropertyAttivitaDTO extends PropertyDTO {

    private String keyDateInizio;

    private LocalDate valueDateInizio;

    private String keyDateFine;

    private LocalDate valueDateFine;

}