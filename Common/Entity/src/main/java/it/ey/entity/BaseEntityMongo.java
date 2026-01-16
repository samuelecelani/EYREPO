package it.ey.entity;


import org.springframework.data.annotation.Id;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public  class BaseEntityMongo {

    @Id
    private String id; // MongoDB ID

    private Long externalId; // ID SQL per collegamento

    private List<Property> properties; // Lista di proprietà dinamiche
}

