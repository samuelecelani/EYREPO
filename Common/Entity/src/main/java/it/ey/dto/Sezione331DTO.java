package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione331DTO extends SezioneBaseDTO {

    private Long id;


    private  Long idPiao;


    private String contesto;
    private String descrizioneQualitativa;
    private String strategiaProgrammazione;
    private String obiettivoTrasformazione;
    private Boolean rimodulazione;
    private String strategiaCopertura;
    private String descrizioneStrategia;
    private String stimaEvoluzione;

    // NON esponiamo idStato
    private String statoSezione;
    private List<TabellaFunzionaleDTO> tabelleFunzionali;




}
