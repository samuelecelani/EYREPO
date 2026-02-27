package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import it.ey.entity.campiTecnici.CampiTecnici;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class FaseDTO extends CampiTecniciDTO {

    private Long id;
    private Long idSezione22;
    @JsonIgnore
    private Long idPiao;
    private String denominazione;

    private String descrizione;

    private String tempi;

    //NoSql
    private AttoreDTO attore;

    private AttivitaDTO  attivita;


}
