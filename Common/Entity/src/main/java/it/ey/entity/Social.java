package it.ey.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "social")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)

public class Social extends BaseEntityMongo{


}
