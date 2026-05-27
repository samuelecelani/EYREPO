package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione32DTO extends SezioneBaseDTO
{
    private Long id;

    private Long idPiao;

    private String statoSezione;

    private String descrizioneContestoRiferimento;

    private String descrizioneObiettiviLavoroAgile;

    private String descrizioneStatoAttuazione;

    private String descrizioneFattoriAbilitanti;

    private String descrizionePersonaleAgile;

    private String descrizioneGiornateLavorate;

    private String descrizioneLivelloSoddisfazione;

    private String descrizioneContributi;

    private String descrizioneImpatti;

    private List<TabellaFunzionaleDTO> tabelleFunzionali;
}
