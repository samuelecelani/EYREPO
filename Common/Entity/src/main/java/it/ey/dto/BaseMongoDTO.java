package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class BaseMongoDTO {
    private String id;
    private Long externalId;
    private List<PropertyDTO> properties;
}

