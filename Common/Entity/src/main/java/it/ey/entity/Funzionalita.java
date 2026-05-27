package it.ey.entity;

import it.ey.dto.FunzionalitaDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Funzionalita")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Funzionalita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NomeFunzionalita", nullable = false)
    private String nomeFunzionalita;

    @Column(name = "DescrizioneFunzionalita")
    private String descrizioneFunzionalita;
    @Column(name = "codicefunzionalita")
    private String codiceFunzionalita;
    @OneToMany(mappedBy = "funzionalita", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FunzionalitaRuolo> funzionalitaByRuoli;


    public Funzionalita(FunzionalitaDTO funzionalita) {
        this.id = funzionalita.getId();
        this.nomeFunzionalita = funzionalita.getNomeFunzionalita();
        this.descrizioneFunzionalita = funzionalita.getDescrizioneFunzionalita();
        this.funzionalitaByRuoli = funzionalita.getFunzionalitaByRuoli().stream()
                .map(FunzionalitaRuolo::new)
                .toList();
    }
}
