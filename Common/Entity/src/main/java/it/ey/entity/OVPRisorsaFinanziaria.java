package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "OVPRisorsaFinanziaria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class OVPRisorsaFinanziaria extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idovp", referencedColumnName = "id", nullable = false)
    private OVP ovp;

    @Column(name = "iniziativa")
    private String iniziativa;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "dotazionefinanziaria")
    private Long dotazioneFinanziaria;

    @Column(name = "fontefinanziamento")
    private String fonteFinanziamento;

}
