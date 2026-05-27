package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "anagrafica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Anagrafica extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "idSezione1", nullable = false, unique = true)
    private Sezione1 idSezione1;

    @Column(name = "denominazioneEnte")
    private String denominazioneEnte;

    @Column(name = "acronimoPa")
    private String acronimoPA;

    @Column(name = "codiceFiscale")
    private String codiceFiscale;

    @Column(name = "codiceIpa")
    private String codiceIPA;

    @Column(name = "tipologiaPa")
    private String tipologiaPA;

    @Column(name = "tipologiaIstat")
    private String tipologiaIstat;

    @Column(name = "piva")
    private String piva;

    @Column(name = "indirizzoSedeLegale")
    private String indirizzoSedeLegale;

    @Column(name = "indirizzoUrp")
    private String indirizzoURP;

    @Column(name = "www")
    private String www;

    @Column(name = "mail")
    private String mail;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "pec")
    private String pec;

    @Column(name = "nomerpct")
    private String nomeRPCT;

    @Column(name = "CognomeRPCT")
    private String cognomeRCTP;

    @Column(name = "RuoloRPCT")
    private String ruoloRPCT;

    @Column(name = "DataNominaRPCT")
    private LocalDate dataNominaRPCT;

    @Column(name = "NomeRTD")
    private String nomeRTD;

    @Column(name = "StrutturaRifRTD")
    private String strutturaRifRTD;

    @Column(name = "social")
    private String social;



}
