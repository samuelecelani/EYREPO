package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * DTO che mappa la risposta del servizio OpenCms:
 * GET /opencms/handle/news-tipologie
 * Response: { "items": [ { "id": "...", "label": "...", "count": 12 } ] }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsTipologieResponseExternalDTO {

    private List<NewsTipologiaExternalDTO> items;
}

