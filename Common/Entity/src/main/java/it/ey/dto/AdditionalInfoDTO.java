package it.ey.dto;

import io.swagger.v3.oas.annotations.media.Schema;


import it.ey.entity.AdditionalInfo;
import it.ey.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalInfoDTO {


    @Schema(description = "ID dell'oggetto", example = "abc123")
    private String id;

    @Schema(description = "ID esterno collegato", example = "456")
    private Long externalId;

    @Schema(description = "Lista di proprietà")
    private List<PropertyDTO> properties;


    public AdditionalInfoDTO(AdditionalInfo entity) {
        if (entity != null) {
            this.id = entity.getId();
            for (Property property : entity.getProperties()){
                this.properties = new ArrayList<>();
                this.properties.add( new PropertyDTO(property) );
            }
            this.externalId = entity.getExternalId();
        }
    }
}
