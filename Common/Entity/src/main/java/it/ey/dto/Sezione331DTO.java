package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione331DTO extends CampiTecniciDTO {

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



}
