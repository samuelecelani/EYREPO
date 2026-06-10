package it.ey.dto.external;

import it.ey.dto.PropertyDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialExternalDTO {

    private List<PropertyDTO> properties;
}
