package it.ey.dto;

import it.ey.enums.TypeAuthority;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private String  nome;
    private String cognome;
    private String email;
    private String username;
    private List<PaRiferimentoDTO> paRiferimento;
    private Date dataNascita;
    private String luogoDiNascita;
    private String fiscalCode;
    private TypeAuthority typeAuthority;




}
