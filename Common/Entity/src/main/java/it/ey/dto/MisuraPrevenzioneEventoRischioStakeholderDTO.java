package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MisuraPrevenzioneEventoRischioStakeholderDTO  extends  CampiTecniciDTO{
    private Long id;

    @JsonIgnore
    private MisuraPrevenzioneEventoRischioDTO misuraPrevenzioneEventoRischioDTO;

    private StakeHolderDTO stakeholder;
}
