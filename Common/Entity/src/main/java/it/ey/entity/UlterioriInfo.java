package it.ey.entity;


import it.ey.enums.Sezione;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "ulteriori_info")
@NoArgsConstructor
@Getter
@Setter
public class UlterioriInfo extends BaseEntityMongo {


    // Campo per distinguere la sezione
    private Sezione tipoSezione;

    public UlterioriInfo(String id, Long externalId, List<Property> properties) {
        super(id, externalId, properties);
    }


}
