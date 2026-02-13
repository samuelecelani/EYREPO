package it.ey.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import it.ey.entity.Test;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Modello dati esterno da usare per api
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TestDTO extends CampiTecniciDTO {


    @Schema(description = "Identificativo del test", example = "123")
    private Long id;

    @Schema(description = "Testo associato al test", example = "Questo è un test")
    private String testo;

    @Schema(description = "Informazioni aggiuntive")
    private AdditionalInfoDTO additionalInfo;



    public TestDTO(Test entity) {
        if (entity != null) {
                this.id = entity.getId();
                this.testo = entity.getTesto();
                this.setCreatedBy(entity.getCreatedBy());
                this.setUpdatedBy(entity.getUpdatedBy());
                this.setValidity(entity.getValidity());
                this.setCreatedTs(entity.getCreatedTs());
                this.setUpdatedTs(entity.getUpdatedTs());

            }
        }
    }



