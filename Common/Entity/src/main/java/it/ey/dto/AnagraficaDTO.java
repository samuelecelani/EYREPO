package it.ey.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnagraficaDTO {
    private Long id;
    private Long idSezione1;
    private String denominazioneEnte;
    private String acronimoPA;
    private String codiceFiscale;
    private String codiceIPA;
    private String tipologiaPA;
    private String piva;
    private String indirizzoSedeLegale;
    private String indirizzoURP;
    private String www;
    private String mail;
    private String telefono;
    private String pec;
    private String nomeRPCT;
    private String cognomeRCTP;
    private String ruoloRPCT;
    private String dataNominaRPCT;
    private String nomeRTD;
    private String strutturaRifRTD;
    private SocialDTO  social;
}

