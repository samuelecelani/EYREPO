package it.ey.entity;

import it.ey.dto.BaseMongoDTO;
import it.ey.dto.PropertyDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "logo")
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)

public class Logo extends BaseEntityMongo {




}
