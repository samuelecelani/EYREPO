package it.ey.dto;


import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class BaseMongoDTO {
    private String id;
    private Long externalId;
    private List<PropertyDTO> properties;
}

