package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import it.ey.entity.StakeHolder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class MisuraPrevenzioneStakeholderDTO extends CampiTecniciDTO{

    private  Long id;

    @JsonIgnore
    private MisuraPrevenzioneDTO misuraPrevenzione ;

    private StakeHolderDTO stakeholder;
}
