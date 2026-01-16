package it.ey.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SwotPuntiForzaDTO extends BaseMongoDTO {



    public SwotPuntiForzaDTO(String id, Long externalId, List<PropertyDTO> properties) {
        super(id, externalId, properties);
    }


}
