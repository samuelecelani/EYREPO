package it.ey.dto;

import it.ey.entity.DimensioneIndicatore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DimensioneIndicatoreDTO extends TipologicaDTO{

    private String codTipologiaFK;

    public DimensioneIndicatoreDTO(DimensioneIndicatore entity) {
        if (entity != null) {
            this.setId(entity.getId());
            this.setValue(entity.getValue());
            this.setCodTipologiaFK(entity.getCodTipologiaFK());
        }
    }
}

