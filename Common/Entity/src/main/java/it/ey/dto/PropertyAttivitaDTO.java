package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Estende PropertyDTO aggiungendo campi specifici per le attività:
 * - keyDataInizio / valueDataInizio
 * - keyDataFine  / valueDataFine
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PropertyAttivitaDTO extends PropertyDTO {

    private String keyDataInizio;
    private LocalDate valueDataInizio;

    private String keyDataFine;
    private LocalDate valueDataFine;


}