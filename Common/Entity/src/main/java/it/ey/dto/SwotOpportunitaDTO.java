package it.ey.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SwotOpportunitaDTO extends BaseMongoDTO {


    public SwotOpportunitaDTO(String id, Long externalId, List<PropertyDTO> properties) {
        super(id, externalId, properties);
    }


}
