package it.ey.dto;

import it.ey.enums.TypeAuthority;
import lombok.*;

import java.time.LocalDate;
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
    private String username;
    private String externalId;
    private List<PaRiferimentoDTO> paRiferimento;
    private LocalDate dataNascita;
    private String luogoDiNascita;
    private String fiscalCode;
    private TypeAuthority typeAuthority;




}
