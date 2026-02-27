package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class PiaoDTO extends  CampiTecniciDTO {
    private Long id;
    private String codPAFK;
    private String denominazione;
    private String versione;
    private String tipologia;
    private String tipologiaOnline;
//    private String statoPiao;
    private List<StakeHolderDTO> stakeHolders;
    private Long idSezione1;
    private Long idSezione21;
    private Long idSezione22;
    private Long idSezione23;
    private Long idSezione31;
    private Long idSezione32;
    private Long idSezione331;
    private Long idSezione332;
    private Long idSezione4;
    private LocalDate dataApprovazione;
    private String url;


}
