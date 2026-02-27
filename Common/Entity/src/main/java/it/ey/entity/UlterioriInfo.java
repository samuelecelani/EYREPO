package it.ey.entity;


import it.ey.enums.Sezione;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "ulteriori_info")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class UlterioriInfo extends BaseEntityMongo {


    // Campo per distinguere la sezione
    private Sezione tipoSezione;



}
