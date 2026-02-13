package it.ey.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "utenteRuoloPa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtenteRuoloPa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codiceFiscale", nullable = false, length = 16)
    private String codiceFiscale;


    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RuoloUtente> ruoli;

    @OneToMany(mappedBy = "utente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UtentePa> codicePA;

    @OneToMany(mappedBy = "utenteRuoloPa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UtenteRuoliPaSezione> sezioni;
}

