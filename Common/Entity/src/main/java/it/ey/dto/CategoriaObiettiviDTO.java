package it.ey.dto;

import it.ey.enums.CodTipologiaCategoria;
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
public class CategoriaObiettiviDTO extends CampiTecniciDTO {

    private Long id;

    // Riferimento alla Sezione4 solo con ID
    private Long idSezione4;

    // Riferimento alla SottofaseMonitoraggio solo con ID
    private Long idSottofase;

    // ID della categoria obiettivi (da capire dove recuperarli)
    private Long idCategoriaObbiettivi;

    // Tipologia categoria (SEZIONE_21 o SEZIONE_22)
    private CodTipologiaCategoria codTipologiaFk;

    // NoSQL
    private UlterioriInfoDTO ulterioriInfo;
    private AttoreDTO attore;
    private AttivitaDTO attivita;
}
