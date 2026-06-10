package it.ey.entity;

import it.ey.enums.Sezione;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "azione")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)

public class Azione extends BaseEntityMongo
{
}
