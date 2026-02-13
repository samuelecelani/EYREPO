package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;



@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AllegatoDTO extends CampiTecniciDTO{

    private Long id;
    private Long idEntitaFK;
    private String codDocumento;
    private String codTipologiaFK;
    private String codTipologiaAllegato;
    private String descrizione;
    private String downloadUrl;
    private String sizeAllegato;
    private String type;
    private Boolean isDoc;
    private String base64;
    private LogoDTO logo;
}

