package it.ey.dto;


import it.ey.entity.Property;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropertyDTO {
    private String key;
    private String value;


    public PropertyDTO(Property entity) {
        this.key = entity.getKey();
        this.value = entity.getValue();
    }
}
