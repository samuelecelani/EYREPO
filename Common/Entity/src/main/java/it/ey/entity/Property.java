package it.ey.entity;

import it.ey.dto.PropertyDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Property {
    private String key;
    private String value;


    public Property(PropertyDTO propertyDTO) {
        this.key = propertyDTO.getKey();
        this.value = propertyDTO.getValue();
    }
}
