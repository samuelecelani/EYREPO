package it.ey.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrincipioGuidaDTO {
    private Long id;
    private Long idSezione1;
    private String nomePrincipioGuida;
    private String descrizionePrincipioGuida;

}
