package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class EventoRischioDTO extends CampiTecniciDTO {


    private Long id;
    private Long idAttivitaSensibile;
    private Long idSezione23;
    private String denominazione;
    private Long probabilita;
    private Long impatto;
    private String controlli;
    private Long valutazione;
    private Long idLivelloRischio;
    private String motivazione;
    private List<MisuraPrevenzioneEventoRischioDTO> misure;
    // NoSql
    private UlterioriInfoDTO ulterioriInfo;
    private FattoreDTO fattore;




}
