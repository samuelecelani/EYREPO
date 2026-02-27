package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


import java.util.List;

@Entity
@Table(name = "Sezione23")
//@Audited
//@AuditTable(value = "Sezione23_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class Sezione23 extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @Column(name = "idStato")
    private Long  idStato;

    @Column(name = "IntroAdempimentiNormativi")
    private String introAdempimentiNormativi;

    @Column(name = "ImpattoContestoExt")
    private String impattoContestoExt;

    @Column(name = "ImpattoContestoInt")
    private String impattoContestoInt;

    @Column(name = "DescrGestioneRischio")
    private String descrGestioneRischio;

    @Column(name = "DescrIdentificazioneRischio")
    private String descrIdentificazioneRischio;

    @Column(name = "DescrAnalisiRischio")
    private String descrAnalisiRischio;

    @Column(name = "DescrMisurazioneRischio")
    private String descrMisurazioneRischio;

    @Column(name = "DescrTrattamentoRischio")
    private String descrTrattamentoRischio;

    @Column(name = "DescrMonitoraggioRischio")
    private String descrMonitoraggioRischio;

    @Column(name = "IntroObiettivoPrevenzione")
    private String introObiettivoPrevenzione;

    @Column(name = "IntroMisurePrevenzione")
    private String introMisurePrevenzione;

    @Column(name = "IntroValorePubblico")
    private String introValorePubblico;

    @Column(name = "IntroAttivitaSensibili")
    private String introAttivitaSensibili;

    @Column(name = "IntroValutazioneRischio")
    private String introValutazioneRischio;

    @Column(name = "IntroGestioneRischio")
    private String introGestioneRischio;

    @Column(name = "IntroMonitoraggio")
    private String introMonitoraggio;

    @Column(name = "DescrTrasparenza")
    private String descrTrasparenza;

    @OneToMany(mappedBy = "sezione23", cascade = CascadeType.REMOVE)
    private List<ObiettivoPrevenzione> obiettivoPrevenzione;

    @OneToMany(mappedBy = "sezione23", cascade = CascadeType.REMOVE)
    @Column(name = "IdSezione23")
    private List<AdempimentiNormativi> adempimentiNormativi;

    @OneToMany(mappedBy = "sezione23", cascade = CascadeType.REMOVE)
    private List<ObbligoLegge> obblighiLegge;

    @OneToMany(mappedBy = "sezione23", cascade = CascadeType.REMOVE)
    private List<AttivitaSensibile> attivitaSensibile;


    @OneToMany(mappedBy = "sezione23", cascade = CascadeType.REMOVE)
    private List<ObiettivoPrevenzioneCorruzioneTrasparenza> obiettivoPrevenzioneCorruzioneTrasparenza;

    @OneToMany(mappedBy = "sezione23", cascade = CascadeType.REMOVE)
    private  List<MisuraPrevenzione> misuraPrevenzione;

    @OneToMany(mappedBy = "sezione23", cascade = CascadeType.REMOVE)
    private List<EventoRischio> eventoRischio;

}

