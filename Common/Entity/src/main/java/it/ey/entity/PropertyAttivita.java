package it.ey.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Estende Property aggiungendo campi specifici per le attività:
 * - keyDataInizio / valueDataInizio
 * - keyDataFine  / valueDataFine
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PropertyAttivita extends Property {

    private String keyDataInizio;
    private LocalDate valueDataInizio;

    private String keyDataFine;
    private LocalDate valueDataFine;

    public PropertyAttivita(String key, String value,
                            String keyDataInizio, LocalDate valueDataInizio,
                            String keyDataFine, LocalDate valueDataFine) {
        super(key, value);
        this.keyDataInizio = keyDataInizio;
        this.valueDataInizio = valueDataInizio;
        this.keyDataFine = keyDataFine;
        this.valueDataFine = valueDataFine;
    }
}
