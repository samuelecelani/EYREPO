package it.ey.dto;

import it.ey.entity.FunzionalitaRuolo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class FunzionalitaRuoloDTO {

    //private Long id;
    private RuoloDTO ruolo;

    // Costruttore che accetta l'entity
    public FunzionalitaRuoloDTO(FunzionalitaRuolo entity) {
        this.ruolo = new RuoloDTO( entity.getRuolo());
    }

}

