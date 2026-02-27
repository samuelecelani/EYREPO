package it.ey.dto;


import it.ey.entity.Property;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PropertyDTO {
    private String key;
    private String value;


    public PropertyDTO(Property entity) {
        this.key = entity.getKey();
        this.value = entity.getValue();
    }
}
