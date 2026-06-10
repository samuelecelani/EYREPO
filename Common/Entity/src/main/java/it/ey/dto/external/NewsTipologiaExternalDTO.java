package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * DTO che mappa un singolo elemento nella risposta di news-tipologie.
 * Campi: id (path completo categoria), label (nome breve), count (numero news).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsTipologiaExternalDTO {

    private String id;
    private String label;
    private Integer count;
}

