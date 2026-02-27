package it.ey.dto;

import it.ey.entity.Funzionalita;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FunzionalitaDTO {

    private Long id;
    private String nomeFunzionalita;
    private String descrizioneFunzionalita;
    private String codiceFunzionalita;
    private List<FunzionalitaRuoloDTO> funzionalitaByRuoli ;


    public FunzionalitaDTO(Funzionalita entity) {
        this.id = entity.getId();
        this.nomeFunzionalita = entity.getNomeFunzionalita();
        this.descrizioneFunzionalita = entity.getDescrizioneFunzionalita();
        this.codiceFunzionalita = entity.getCodiceFunzionalita();
        this.funzionalitaByRuoli = entity.getFunzionalitaByRuoli().stream()
                .map(fr -> {
                    FunzionalitaRuoloDTO dto = new FunzionalitaRuoloDTO();
                    dto.setRuolo(new RuoloDTO(fr.getRuolo()));
                    return dto;
                }).toList();
    }


}
