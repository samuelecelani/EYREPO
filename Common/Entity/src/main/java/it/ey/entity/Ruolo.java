package it.ey.entity;

import it.ey.dto.RuoloDTO;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Ruolo")
public class Ruolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codRuolo", nullable = false, unique = true)
    private String codRuolo;

    @Column(name = "descrizione")
    private String descrizione;


    public Ruolo(RuoloDTO dto) {
        //this.id = dto.getId();
        this.codRuolo = dto.getCodRuolo();
        this.descrizione = dto.getDescrizione();
    }
}