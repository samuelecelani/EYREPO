package it.ey.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AvvisoDTO extends CampiTecniciDTO {
    private Long id;
    private String tipoAvviso;
    private String titolo;
    private String intro;
    private String descrizione;
    private Boolean visualizzato;
}

