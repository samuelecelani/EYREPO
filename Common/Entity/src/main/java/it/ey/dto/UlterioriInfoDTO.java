package it.ey.dto;


import it.ey.enums.Sezione;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor

public class UlterioriInfoDTO extends BaseMongoDTO {
    private Sezione tipoSezione;

    public UlterioriInfoDTO(String id, Long externalId, List<PropertyDTO> properties) {
        super(id, externalId, properties);

    }


}
