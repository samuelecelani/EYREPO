package it.ey.dto;

import it.ey.entity.TargetIndicatore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TargetIndicatoreDTO extends TipologicaDTO{

    public TargetIndicatoreDTO(TargetIndicatore entity) {
        if (entity != null) {
            this.setId(entity.getId());
            this.setValue(entity.getValue());
        }
    }
}
