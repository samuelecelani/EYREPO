package it.ey.dto;


import it.ey.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PropertyDTO {
    private String key;
    private String value;


    public PropertyDTO(Property entity) {
        this.key = entity.getKey();
        this.value = entity.getValue();
    }
}
