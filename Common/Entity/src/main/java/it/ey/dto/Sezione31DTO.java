package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione31DTO extends SezioneBaseDTO
{
    private Long id;

    private Long idPiao;

    private String statoSezione;

    private String strutturaOrganizzativaAP;

    //commento
    private String ampiezzaOrganica;

    private String incarichiDirigenziali;

    private String profiliProfessionali;

    private String lineeOrganizzazione;

    private List<AmpiezzaOrganizzativaDTO> ampiezzaOrganizzative;

    private List<TabellaFunzionaleDTO> tabelleFunzionali;

    private Boolean graficoMinerva;
}