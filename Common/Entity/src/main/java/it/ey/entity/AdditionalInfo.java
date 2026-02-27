package it.ey.entity;
import it.ey.dto.AdditionalInfoDTO;
import it.ey.dto.PropertyDTO;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "TestM")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdditionalInfo {
    @Id
    private String id;
    private Long externalId;
    private List<Property> properties;
    public AdditionalInfo(AdditionalInfoDTO additionalInfoDTO) {
        if (additionalInfoDTO != null) {
            this.properties = new ArrayList<>();
            for (PropertyDTO propertyDTO : additionalInfoDTO.getProperties()) {
                this.properties.add(new Property(propertyDTO));
            }
            this.externalId = additionalInfoDTO.getExternalId();
        }
    }

}
