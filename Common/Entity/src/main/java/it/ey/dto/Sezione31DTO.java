package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione31DTO extends CampiTecniciDTO
{
    private Long id;

    private Long idPiao;

    private String statoSezione;

    private String strutturaOrganizzativaAP;

    private String ampiezzaOrganica;

    private String incarichidirigenziali;

    private String profiliProfessionali;

    private String lineeOrganizzazione;
}
