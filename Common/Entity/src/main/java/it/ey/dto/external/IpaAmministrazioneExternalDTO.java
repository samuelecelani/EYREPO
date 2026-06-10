package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * DTO che mappa la risposta del web service IPA - DFP:
 * GET https://ac-api-coll.dfp.gov.it/api/Public/GetAdministrationsInfoForCodiceFiscale?filter={codiceFiscale}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaAmministrazioneExternalDTO {

    private String nomeResponsabile;
    private String cognomeResponsabile;
    private String acronimo;
    private String cFValidato;
    private String livAccessibili;
    private String titoloResponsabile;
    private String tipologiaIstat;
    private String tipologiaAmministrazione;
    private List<IpaAreaOrganizzativaOmogeneaExternalDTO> areeOrganizzativeOmogenee;
    private String altro;
    private String facebook;
    private String sitoIstituzionale;
}

