package it.ey.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "attivita")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class Attivita extends BaseEntityMongo {

    /**
     * Sovrascrive il campo properties di BaseEntityMongo
     * per usare PropertyAttivita (con campi data inizio/fine)
     */
    private List<PropertyAttivita> propertyAttivita;

}
