package it.ey.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrioritaPoliticaDTO {
    private Long id;
    private Long idSezione1;
    private String nomePrioritaPolitica;
    private String descrizionePrioritaPolitica;
}