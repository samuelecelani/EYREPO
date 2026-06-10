package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaUnitaOrganizzativaExternalDTO {

    private String codiceUo;
    private String codiceAoo;
    private String codiceAmministrazione;
    private String codiceUoPadre;
    private String codiceUnivocoUo;
    private String descrizioneUo;
    private String indirizzo;
    private String comune;
    private String cap;
    private String provincia;
    private String regione;
    private String telefono;
    private String fax;
    private String nomeResponsabile;
    private String cognomeResponsabile;
    private String telefonoResponsabile;
    private String mailResponsabile;
    private String mail1;
    private Integer tipoMail;
    private String mail2;
    private Integer tipoMail2;
    private String mail3;
    private Integer tipoMail3;
}

