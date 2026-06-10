package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class ObiettiviRisultatiFotografiaDTO extends BaseDTO {

    private Long id;

    private Long idSezione332;
    private Long idTipologiaAttivita;
    private Long idAmbitoCompetenza;
    private Long idAreaTematica;
    private Long idTipologiaDestinatari;

    private String codTipologiaFK;

    private String codice;
    private String titolo;
    private Boolean carattereObbligatorio;
    private String riferimentoNormativo;
    private String targetDirigenti;
    private String targetNonDirigenti;

    private Long numeroDirigenti;
    private Long numeroNonDirigenti;

    private Double oreFormazione;

    private Boolean verificaApprendimento;
    private Double creditiFormativi;

    private String modalitaGestioneFormazione;
    private String enteErogatore;

    private String costoAttivita;

    private LocalDate dataInizio;
    private LocalDate dataFine;
}
