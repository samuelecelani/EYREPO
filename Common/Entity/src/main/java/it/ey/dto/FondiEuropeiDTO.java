package it.ey.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FondiEuropeiDTO extends CampiTecniciDTO{

    private Long id;
    private String progettoFinanziato;
    private String descrizione;
    private Double fondiStanziati;


}

