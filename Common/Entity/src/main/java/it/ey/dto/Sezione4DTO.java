package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione4DTO  extends  CampiTecniciDTO{

    private Long id;
    private Long idPiao;
    private String statoSezione;
    private String descrStrumenti;
    private String descrModalitaRilevazione;
    private String intro;
    private String intro21;
    private String intro22;
    private String descr22;
    private String descr23;
    private String descr31;
    private String descr32;
    private String descr331;
    private String descr332;
    private String descrMonitoraggio;
    private List <SottofaseMonitoraggioDTO> sottofaseMonitoraggio;
    private List<CategoriaObiettiviDTO> categoriaObiettivi;
    //NoSQL
    private UlterioriInfoDTO ulterioriInfo;
    private AttoreDTO attore;

}
