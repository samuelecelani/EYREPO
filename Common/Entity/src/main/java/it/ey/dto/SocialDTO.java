package it.ey.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SocialDTO extends BaseMongoDTO {



    public SocialDTO(String id, Long externalId, List<PropertyDTO> properties) {
        super(id, externalId, properties);
    }

}

