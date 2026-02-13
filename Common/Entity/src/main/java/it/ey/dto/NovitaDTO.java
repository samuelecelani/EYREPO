package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovitaDTO {
    private Long id;
    private String tipoNovita;
    private String titolo;
    private String intro;
    private String descrizione;
}