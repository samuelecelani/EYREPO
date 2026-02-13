package it.ey.entity;

import it.ey.enums.Sezione;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "attore")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder(toBuilder = true)
@Setter
public class Attore extends  BaseEntityMongo {
    // Campo per distinguere la sezione
    private Sezione tipoSezione;
    private Long idPiao;
    public Attore(String id, Long externalId, List<Property> properties) {

        super(id, externalId, properties);
    }
}
