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
    private String externalUserId;

    private String idAmministrazione;

    private StrutturaPiaoDTO strutturaPiao;

    private String codiceRuolo;
    private String descrizioneRuolo;


}

