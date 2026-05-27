package it.ey.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AttivitaFormativeDTO extends BaseDTO
{
    private Long id;

    private Long idSezione332;

    private Long idTipologiaAttivita;

    private Long idAmbitoCompetenza;

    private Long idAreaTematica;

    private Long numeroDirigenti;

    private Long numeroNonDirigenti;

    private Double oreFormazione;

    private Boolean verificaApprendimento;
}
