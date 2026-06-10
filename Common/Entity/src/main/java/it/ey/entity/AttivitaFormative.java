package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "AttivitaFormative")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AttivitaFormative extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione332", nullable = false)
    private Sezione332 sezione332;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdTipologiaAttivita", nullable = false)
    private TipologiaAttivita tipologiaAttivita;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdAmbitoCompetenza", nullable = false)
    private AmbitoCompetenza ambitoCompetenza;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdAreaTematica", nullable = false)
    private AreaTematica areaTematica;

    @Column(name = "NumeroDirigenti")
    private Long numeroDirigenti;

    @Column(name = "NumeroNonDirigenti")
    private Long numeroNonDirigenti;

    @Column(name = "OreFormazione")
    private Double oreFormazione;

    @Column(name = "VerificaApprendimento ")
    private Boolean verificaApprendimento;
}