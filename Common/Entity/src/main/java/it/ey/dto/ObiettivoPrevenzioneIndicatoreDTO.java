package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import it.ey.entity.ObbiettivoPerformance;
import it.ey.entity.ObiettivoPrevenzione;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObiettivoPrevenzioneIndicatoreDTO {

    private Long id;


    @JsonIgnore
    private ObiettivoPrevenzione obiettivoPrevenzione;


    private IndicatoreDTO indicatore;

}
