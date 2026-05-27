package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "TabellaFunzionale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class TabellaFunzionale extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identitafk", nullable = false)
    private Long idEntitaFK;

    @Column(name = "codtipologiafk", nullable = false)
    private String codTipologiaFK;

    @Column(name = "codice")
    private String codice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idovp")
    private OVP ovp;

    @Column(name = "denominazionesintetica")
    private String denominazioneSintetica;

    @Column(name = "responsabileamministrativo")
    private String responsabileAmministrativo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idstakeholder")
    private StakeHolder stakeholder;

    @Column(name = "dimensioni")
    private String dimensioni;

    @Column(name = "formula")
    private String formula;

    @Column(name = "polarita")
    private String polarita;

    @Column(name = "baseline")
    private String baseline;

    @Column(name = "targetannon1")
    private String targetAnnoN1;

    @Column(name = "targetannon2")
    private String targetAnnoN2;

    @Column(name = "targetannon3")
    private String targetAnnoN3;

    @Column(name = "fonte")
    private String fonte;
}
