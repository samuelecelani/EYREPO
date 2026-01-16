package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllegatoDTO {

    private Long id;
    private Long idEntitaFK;
    private String codDocumento;
    private String codTipologiaFK; // puoi usare String o Enum
    private String codTipologiaAllegato; // idem
    private String descrizione;
    private String downloadUrl;
    private String sizeAllegato;
}

