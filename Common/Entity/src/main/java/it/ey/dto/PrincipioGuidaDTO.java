package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PrincipioGuidaDTO extends BaseDTO {
    private Long id;
    private Long idSezione1;
    private String nomePrincipioGuida;
    private String descrizionePrincipioGuida;

}
