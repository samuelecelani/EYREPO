package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;

/**
 * Elemento dell'array "notStructuredData" nella risposta DFP2.
 * Il campo notStructuredData annidato è polimorfico (schema diverso per ogni source:
 * BDAP, IPA, Istat, RGS, ecc.) quindi è lasciato come Map generica.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaNotStructuredDataExternalDTO {
    private String source;
    private String administrationCode;
    private Map<String, Object> notStructuredData;
}

