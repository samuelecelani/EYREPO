package it.ey.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganoPoliticoDTO {
    private Long id;
    private Long idSezione1;
    private String organo;
    private String ruolo;
}
