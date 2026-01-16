package it.ey.dto;

import lombok.*;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PiaoDTO extends  CampiTecniciDTO {
    private Long id;
    private String codPAFK;
    private String denominazione;
    private String versione;
    private String tipologia;
    private String tipologiaOnline;
//    private String statoPiao;
    private List<StakeHolderDTO> stakeHolders;
    private Sezione1DTO sezione1;
    private Sezione21DTO sezione21;
    private Sezione22DTO sezione22;
    private Sezione23DTO sezione23;
    private Sezione31DTO sezione31;
    private Sezione32DTO sezione32;
    private Sezione331DTO sezione331;
    private Sezione332DTO sezione332;


}
