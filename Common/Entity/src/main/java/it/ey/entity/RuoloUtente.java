package it.ey.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ruoloUtente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuoloUtente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codiceRuolo", nullable = false)
    private String codiceRuolo;

    @ManyToOne
    @JoinColumn(name = "idUtenteRuoloPa")
    private UtenteRuoloPa utente;
}

