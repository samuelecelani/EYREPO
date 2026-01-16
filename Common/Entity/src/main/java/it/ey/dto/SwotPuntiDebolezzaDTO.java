package it.ey.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "swot_punti_debolezza")
@NoArgsConstructor
@Getter
@Setter
public class SwotPuntiDebolezzaDTO extends BaseMongoDTO {


    public SwotPuntiDebolezzaDTO(String id, Long externalId, List<PropertyDTO> properties) {
        super(id, externalId, properties);
    }


}
