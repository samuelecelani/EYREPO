package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.ey.enums.StatoDichiarazioneEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StoricoDichiarazioneDFPDTO
{
    private Long id;
    private String codePA;
    private String amministrazione;

    private LocalDate dataRicezione;

    private LocalDate dataPrevistaPubblicazione;

    private Boolean stato;

    /** Stato della dichiarazione: INVIATA / NON_INVIATA. */
    private StatoDichiarazioneEnum statoDichiarazione;

    /** Tipologia ISTAT della PA (es. "Comune", "Regione", ...) ricavata dall'Anagrafica. */
    private String tipologiaIstat;
}

