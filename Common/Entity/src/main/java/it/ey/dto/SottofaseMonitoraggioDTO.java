package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class SottofaseMonitoraggioDTO extends CampiTecniciDTO {

    private Long id;

    // Riferimento alla Sezione4 solo con ID
    private Long idSezione4;

    private String denominazione;
    private String descrizione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private String strumenti;
    private String fonteDato;

    private List<MilestoneDTO> milestone;

    // NoSQL
    private AttoreDTO attore;
}