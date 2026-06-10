package it.ey.entity;

import it.ey.dto.PropertyDTO;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Property {
    private String key;
    private String value;

    private Boolean active = true; // Soft delete: true = attivo, false = cancellato logicamente

    private LocalDateTime deactivationTime; // Timestamp della disattivazione

    public Property(PropertyDTO propertyDTO) {
        this.key = propertyDTO.getKey();
        this.value = propertyDTO.getValue();
        this.active = true;
    }

    public Property(String key, String value) {
        this.key = key;
        this.value = value;
        this.active = true;
    }
}
