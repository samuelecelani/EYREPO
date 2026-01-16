package it.ey.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "social")
@Getter
@Setter
@NoArgsConstructor
public class Social extends BaseEntityMongo{

    public Social(String id, Long externalId, List<Property> properties) {
        super(id, externalId, properties);
    }

}
