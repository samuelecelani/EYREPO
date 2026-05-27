package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class TabellaFunzionaleDTO extends BaseDTO {
    private Long id;
    private Long idEntitaFK;
    private String codTipologiaFK;
    private String codice;
    private Long idOVP;
    private String denominazioneSintetica;
    private String responsabileAmministrativo;
    private Long idStakeholder;
    private String dimensioni;
    private String formula;
    private String polarita;
    private String baseline;
    private String targetAnnoN1;
    private String targetAnnoN2;
    private String targetAnnoN3;
    private String fonte;
}
