package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaSettoreExternalDTO {
    private Long id;
    private String descrizione;
    private String sourceTypeName;
}

