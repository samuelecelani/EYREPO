package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * DTO che mappa la risposta del servizio OpenCms:
 * POST /opencms/handle/news-search
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsSearchResponseExternalDTO {

    private Integer page;
    private Integer limit;
    private Integer total;
    private List<NewsItemExternalDTO> items;
}

