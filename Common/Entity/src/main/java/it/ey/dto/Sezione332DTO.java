package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione332DTO extends SezioneBaseDTO{

    private Long id;

    private Long idPiao;

    private String statoSezione;

    private String contestoNormativo;

    private String descrizioneQualitativa;

    private String descrizioneStrategia;

    private String descrizioneRisorse;

    private String descrizioneIncentivi;

    private List<ObiettiviRisultatiFotografiaDTO> obiettiviRisultatiFotografia;

    private List<AttivitaFormativeDTO> attivitaFormative;

    private List<TabellaFunzionaleDTO> tabelleFunzionali;
}

