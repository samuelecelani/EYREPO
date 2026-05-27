package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.ey.entity.ObbiettivoPerformance;
import it.ey.entity.StakeHolder;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObiettivoStakeHolderDTO {


    private Long id;

    @JsonIgnore
    private ObbiettivoPerformance obbiettivoPerformance;

    private StakeHolderDTO stakeholder;

}
