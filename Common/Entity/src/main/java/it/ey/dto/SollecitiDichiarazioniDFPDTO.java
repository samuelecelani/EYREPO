package it.ey.dto;

import it.ey.enums.StatoDichiarazioneEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * DTO ritornato dal search dei solleciti delle dichiarazioni DFP.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SollecitiDichiarazioniDFPDTO
{
    /** Id del PIAO. */
    private Long idPiao;

    /** Codice PA (codPAFK del PIAO). */
    private String codePA;

    /** Denominazione PA (amministrazione) presa dal PIAO. */
    private String amministrazione;

    /** Stato della dichiarazione: INVIATA / NON_INVIATA. */
    private StatoDichiarazioneEnum statoDichiarazione;
}

