package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "Sezione32")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione32 extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @Column(name = "idStato")
    private Long idStato;

    @Column(name = "DescrizioneContestoRiferimento")
    private String descrizioneContestoRiferimento;

    @Column(name = "DescrizioneObiettiviLavoroAgile")
    private String descrizioneObiettiviLavoroAgile;

    @Column(name = "DescrizioneStatoAttuazione")
    private String descrizioneStatoAttuazione;

    @Column(name = "DescrizioneFattoriAbilitanti")
    private String descrizioneFattoriAbilitanti;

    @Column(name = "DescrizionePersonaleAgile")
    private String descrizionePersonaleAgile;

    @Column(name = "DescrizioneGiornateLavorate")
    private String descrizioneGiornateLavorate;

    @Column(name = "DescrizioneLivelloSoddisfazione")
    private String descrizioneLivelloSoddisfazione;

    @Column(name = "DescrizioneContributi")
    private String descrizioneContributi;

    @Column(name = "DescrizioneImpatti")
    private String descrizioneImpatti;
}
