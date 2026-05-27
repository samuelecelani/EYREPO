package it.ey.dto;

import it.ey.entity.BaseEntityMongo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;


@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class FattoreDTO extends BaseMongoDTO {
}
