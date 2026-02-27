package it.ey.dto;

import it.ey.entity.Sezione22;
import it.ey.enums.TipologiaAdempimento;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdempimentoDTO extends  CampiTecniciDTO
{
    private Long id;

    private Long idSezione22;

    private TipologiaAdempimento tipologia;

    private String denominazione;

    private AzioneDTO azione;

    private UlterioriInfoDTO ulterioriInfo;
}
