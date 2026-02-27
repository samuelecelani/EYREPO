package it.ey.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.http.codec.multipart.FilePart;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class LogoDTO extends  BaseMongoDTO{


    public LogoDTO (String id,  Long externalId, List<PropertyDTO> properties) {
        super(id, externalId, properties);
    }


}
