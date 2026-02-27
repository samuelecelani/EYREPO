package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class FondiEuropeiDTO extends CampiTecniciDTO{

    private Long id;
    private String progettoFinanziato;
    private String descrizione;
    private Double fondiStanziati;
    private String idSezione21;


}

