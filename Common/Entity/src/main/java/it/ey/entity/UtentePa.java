package it.ey.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "UtentePa")
public class UtentePa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codicePa", nullable = false)
    private String codicePa;

    @ManyToOne
    @JoinColumn(name = "idUtenteRuoloPa")
    private UtenteRuoloPa utente;
}


