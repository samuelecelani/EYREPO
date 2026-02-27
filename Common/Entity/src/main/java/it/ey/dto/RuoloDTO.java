package it.ey.dto;


import it.ey.entity.Ruolo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RuoloDTO {

    //private Long id;
    private String codRuolo;
    private String descrizione;

    public RuoloDTO(Ruolo ruolo) {
        this.codRuolo = ruolo.getCodRuolo();
        this.descrizione = ruolo.getDescrizione();
    }
}
