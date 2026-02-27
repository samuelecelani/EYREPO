package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AttivitaDTO extends BaseMongoDTO {

    /**
     * Lista di proprietà specifiche per le attività
     * (con campi data inizio/fine)
     */
    private List<PropertyAttivitaDTO> propertyAttivita;

}
