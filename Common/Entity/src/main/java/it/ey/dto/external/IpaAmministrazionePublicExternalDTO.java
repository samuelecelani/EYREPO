package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

/**
 * DTO che mappa un elemento della risposta del web service IPA - DFP2:
 * POST https://ac-api-coll.dfp.gov.it/api/Public
 * Body: { "fiscalCode": "..." }
 * La risposta è un array di oggetti di questo tipo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaAmministrazionePublicExternalDTO {

    private String uniqueCodeDfp;
    private String name;
    private String creationDate;
    private String lastUpdateDate;
    private String fiscalCode;
    private String administrationSourceCode;
    private String region;
    private String district;
    private String city;
    private String cap;
    private String address;
    private String primarySourceType;
    private String state;
    private String email;
    private List<IpaNotStructuredDataExternalDTO> notStructuredData;
    private List<IpaTagExternalDTO> tags;
    private List<IpaCompartoExternalDTO> comparti;
    private List<IpaAooPublicExternalDTO> areeOrganizzativeOmogenee;
    private List<Object> administrationChilds;
    private List<Object> administrationParents;
}

