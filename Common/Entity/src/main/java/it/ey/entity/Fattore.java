package it.ey.entity;

import it.ey.enums.Sezione;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fattore")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class Fattore extends BaseEntityMongo {

    // Campo per distinguere la sezione
    private Sezione tipoSezione;
}
