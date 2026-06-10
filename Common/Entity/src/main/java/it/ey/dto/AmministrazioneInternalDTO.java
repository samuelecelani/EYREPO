package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AmministrazioneInternalDTO {

    private Long id;
    private String denominazioneEnte;
    private String codiceIPA;
    private String tipologiaIstat;
    private String codiceFiscale;
    private String mail;
    private String tipologiaPA;

}
