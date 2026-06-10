package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StoricoModificaDTO extends BaseDTO {

    private Long id;
    private Long idPiao;
    private Long idSezione;
    private String codTipologiaFK;
    private String nomeCognome;
    private String profilo;
    private LocalDate dataModifica;
    private String sezione;
    private String testoSezione;
    private String campiModificati;
}
