package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * DTO della history di AOO / UO nella risposta IPA - DFP2.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaAooUoHistoryExternalDTO {
    private Long id;
    private String descrizione;
    private String dataCreazione;
    private String creatoDa;
}

