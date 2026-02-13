package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtenteRuoliPaSezioneDTO {

    private Long id;
    @JsonIgnore
    private UtenteRuoloPaDTO utenteRuoloPa;

    private StrutturaPiaoDTO strutturaPiao;

}

