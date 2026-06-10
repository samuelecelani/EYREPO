package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AvvisoDTO extends BaseDTO {
    private Long id;
    private String tipologiaContenuto;
    private LocalDate dataPubblicazione;
    private String oggetto;
    private String tipologiaAmministrazione;
    private String amministrazione;
    private String messaggio;
    private String stato;
}
