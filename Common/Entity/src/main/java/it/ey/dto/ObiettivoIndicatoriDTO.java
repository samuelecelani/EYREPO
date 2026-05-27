package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.ey.entity.Indicatore;
import it.ey.entity.ObbiettivoPerformance;
import it.ey.entity.StakeHolder;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObiettivoIndicatoriDTO {


    private Long id;

    @JsonIgnore
    private ObbiettivoPerformanceDTO obbiettivoPerformance;

    private IndicatoreDTO indicatore;

}
