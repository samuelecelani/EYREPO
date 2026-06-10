package it.ey.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IpaAooPublicExternalDTO {
    private Long id;
    private String denominazioneAoo;
    private String codiceAoo;
    private String fonte;
    private IpaStatoExternalDTO statoAoo;
    private List<IpaUoPublicExternalDTO> unitaOrganizzative;
    private List<IpaAooUoHistoryExternalDTO> historyAoo;
}


