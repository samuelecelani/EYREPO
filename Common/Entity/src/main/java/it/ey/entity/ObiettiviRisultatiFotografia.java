package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "ObiettiviRisultatiFotografia")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class ObiettiviRisultatiFotografia extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSezione332")
    private Sezione332 sezione332;

    @Column(name = "CodTipologiaFK")
    private String codTipologiaFK;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdTipologiaAttivita")
    private TipologiaAttivita tipologiaAttivita;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdAmbitoCompetenza")
    private AmbitoCompetenza ambitoCompetenza;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdAreaTematica")
    private AreaTematica areaTematica;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdTipologiaDestinatari")
    private TipologiaDestinatari tipologiaDestinatari;

    @Column(name = "Codice")
    private String codice;

    @Column(name = "Titolo", columnDefinition = "TEXT")
    private String titolo;

    @Column(name = "CarattereObbligatorio")
    private Boolean carattereObbligatorio;

    @Column(name = "RiferimentoNormativo", columnDefinition = "TEXT")
    private String riferimentoNormativo;

    @Column(name = "TargetDirigenti")
    private String targetDirigenti;

    @Column(name = "TargetNonDirigenti")
    private String targetNonDirigenti;

    @Column(name = "NumeroDirigenti")
    private Long numeroDirigenti;

    @Column(name = "NumeroNonDirigenti")
    private Long numeroNonDirigenti;

    @Column(name = "OreFormazione")
    private Double oreFormazione;

    @Column(name = "VerificaApprendimento")
    private Boolean verificaApprendimento;

    @Column(name = "CreditiFormativi")
    private Double creditiFormativi;

    @Column(name = "ModalitaGestioneFormazione")
    private String modalitaGestioneFormazione;

    @Column(name = "EnteErogatore", columnDefinition = "TEXT")
    private String enteErogatore;

    @Column(name = "CostoAttivita")
    private String costoAttivita;

    @Column(name = "DataInizio")
    private LocalDate dataInizio;

    @Column(name = "DataFine")
    private LocalDate dataFine;
}
