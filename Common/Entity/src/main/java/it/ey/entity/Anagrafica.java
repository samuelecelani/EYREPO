package it.ey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "anagrafica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anagrafica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idSezione1", nullable = false)
    private Long idSezione1;

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
    private String dataNominaRPCT;

    @Column(name = "NomeRTD")
    private String nomeRTD;

    @Column(name = "StrutturaRifRTD")
    private String strutturaRifRTD;

    @OneToOne
    @JoinColumn(name = "IdSezione1", nullable = false, unique = true)
    private Sezione1 sezione1;

}
