package it.ey.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "swot_minacce")
@NoArgsConstructor
@Getter
@Setter
public class SwotMinacce extends BaseEntityMongo {


    public SwotMinacce(String id, Long externalId, List<Property> properties) {
        super(id, externalId, properties);
    }


}
