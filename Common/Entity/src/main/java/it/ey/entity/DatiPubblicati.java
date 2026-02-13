package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "DatiPubblicati")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class DatiPubblicati  extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdObbligoLegge", nullable = false)
    private ObbligoLegge obbligoLegge;

    @Column(name = "Denominazione", nullable = false)
    private String denominazione;

    @Column(name = "Tipologia", nullable = false)
    private String tipologia;

    @Column(name = "Responsabile")
    private String responsabile;

    @Column(name = "TerminiScadenza")
    private String terminiScadenza;

    @Column(name = "ModalitaMonitoraggio")
    private String modalitaMonitoraggio;

    @Column(name = "MotivazioneImpossibilita")
    private String motivazioneImpossibilita;
}
