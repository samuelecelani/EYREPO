package it.ey.dto;


import it.ey.entity.EventoRischio;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AttivitaSensibileDTO  extends  CampiTecniciDTO{

    private Long id;

    private Long idSezione23;

    private String denominazione;

    private String descrizione;

    private String processoCollegato;

    private List<EventoRischioDTO> eventoRischio;

       //CAMPI MONGO

    private AttoreDTO attore;

    private   UlterioriInfoDTO ulterioriInfo;
}
