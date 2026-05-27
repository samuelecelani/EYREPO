package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO che mappa un singolo item nella risposta di news-search.
 * Campi: id (UUID OpenCms), titolo, abstract, data (ISO-8601), tipologia.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsItemExternalDTO {

    private String id;
    private String titolo;

    @JsonProperty("abstract")
    private String abstractText;

    private String data;
    private String tipologia;
}

