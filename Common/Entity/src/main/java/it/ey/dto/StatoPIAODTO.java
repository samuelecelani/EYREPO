package it.ey.dto;


import it.ey.entity.StatoPIAO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatoPIAODTO {
    private Long id;

    private String testo;


    public  StatoPIAODTO(StatoPIAO entity) {
        this.id = entity.getId();
        this.testo = entity.getTesto();
    }
}

