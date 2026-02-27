package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OVPStakeHolderDTO {
    private Long id;
    @JsonIgnore
    private OVPDTO ovp;
    private StakeHolderDTO stakeholder;

}
