package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "obbligolegge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class ObbligoLegge extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSezione23", nullable = false)
    private Sezione23 sezione23;

    @Column(name = "Denominazione", nullable = false, length = 255)
    private String denominazione;

    @Column(name = "Descrizione")
    private String descrizione;

    @OneToMany(mappedBy = "obbligoLegge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatiPubblicati> datiPubblicati;


}
