package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PrioritaPoliticaDTO extends BaseDTO{
    private Long id;
    private Long idSezione1;
    private String nomePrioritaPolitica;
    private String descrizionePrioritaPolitica;
}
