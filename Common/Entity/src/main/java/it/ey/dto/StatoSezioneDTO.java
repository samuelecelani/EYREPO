package it.ey.dto;


import it.ey.entity.StatoSezione;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatoSezioneDTO {
    private Long id;

    private String testo;


    public  StatoSezioneDTO(StatoSezione entity) {
        this.id = entity.getId();
        this.testo = entity.getTesto();
    }
}
