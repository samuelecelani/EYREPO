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

    private String keyDateFine;

    private LocalDate valueDateFine;

    private String keyDateInizio;

    private LocalDate valueDateInizio;

    public PropertyAttivita(String key, String value,
                            String keyDateFine, LocalDate valueDateFine,
                            String keyDateInizio, LocalDate valueDateInizio) {
        super(key, value);
        this.keyDateFine = keyDateFine;
        this.valueDateFine = valueDateFine;
        this.keyDateInizio = keyDateInizio;
        this.valueDateInizio = valueDateInizio;
    }
}
