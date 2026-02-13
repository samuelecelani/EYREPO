package it.ey.dto;


import it.ey.enums.Sezione;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AttoreDTO extends BaseMongoDTO {
    private Sezione tipoSezione;
    private Long idPiao;

    public AttoreDTO(String id, Long externalId, List<PropertyDTO> properties) {
        super(id, externalId, properties);
    }
}
