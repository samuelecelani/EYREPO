package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MisuraPrevenzioneIndicatoreDTO extends CampiTecniciDTO{

    private Long id;


    @JsonIgnore
    private MisuraPrevenzioneDTO misuraPrevenzione;


    private IndicatoreDTO indicatore;

}
