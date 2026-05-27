package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovitaDTO {
    private String id;
    private String tipologia;
    private String titolo;
    private String abstractText;
    private String data;
    private String testoHtml;
}