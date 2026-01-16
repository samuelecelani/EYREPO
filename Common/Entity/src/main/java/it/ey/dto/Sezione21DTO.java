package it.ey.dto;


import lombok.*;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sezione21DTO  extends CampiTecniciDTO{

    private Long id;
    private Long idPiao;
    private Long idStato;
    private String contestoInt;
    private String contestoExt;
    private String descrizioneValorePubblico;
    private String descrizioneAccessiDigitale;
    private String descrizioneAccessiFisica;
    private String descrizioneSemplificazione;
    private String descrizionePariOpportunita;
    private String statoSezione;
    private List<FondiEuropeiDTO> fondiEuropei;
    private UlterioriInfoDTO ulterioriInfo;
    private SwotPuntiForzaDTO swotPuntiForza;
    private SwotOpportunitaDTO swotOpportunita;
    private SwotPuntiDebolezzaDTO swotPuntiDebolezza;
    private SwotMinacceDTO swotMinacce;
    private List<OVPDTO> ovp;
    private List<ProceduraDTO> procedure;
    private List<OVPStrategiaIndicatoreDTO> strategiaDTOS;





}

