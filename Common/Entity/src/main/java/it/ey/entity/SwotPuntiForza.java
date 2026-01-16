package it.ey.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "swot_punti_forza")
@NoArgsConstructor
@Getter
@Setter
public class SwotPuntiForza extends BaseEntityMongo {



    public SwotPuntiForza(String id, Long externalId, List<Property> properties) {
        super(id, externalId, properties);
    }


}
