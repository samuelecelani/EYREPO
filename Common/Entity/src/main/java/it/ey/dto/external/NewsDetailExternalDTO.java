package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO che mappa la risposta del servizio OpenCms:
 * GET /opencms/handle/news-detail?id={UUID}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsDetailExternalDTO {

    private String id;
    private String titolo;

    @JsonProperty("abstract")
    private String abstractText;

    private String data;
    private String testoHtml;
    private String tipologia;
}

