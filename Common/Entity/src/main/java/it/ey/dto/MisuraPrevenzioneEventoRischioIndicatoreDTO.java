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
public class MisuraPrevenzioneEventoRischioIndicatoreDTO extends BaseDTO{
    private Long id;

    @JsonIgnore
    private MisuraPrevenzioneEventoRischioDTO misuraPrevenzioneEventoRischioDTO;
    private IndicatoreDTO indicatore;
}
