package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaAreaOrganizzativaOmogeneaExternalDTO {

    private String codiceAmministrazione;
    private String codiceAoo;
    private String descrizioneAoo;
    private String dataIstituzionale;
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
    private List<IpaUnitaOrganizzativaExternalDTO> unitaOrganizzative;
}

