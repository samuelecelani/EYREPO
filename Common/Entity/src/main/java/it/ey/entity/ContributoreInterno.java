package it.ey.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "contributore_interno")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)

public class ContributoreInterno extends BaseEntityMongo{

}
