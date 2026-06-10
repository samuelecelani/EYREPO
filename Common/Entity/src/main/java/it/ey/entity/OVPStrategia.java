package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Table(name = "OVPStrategia")
//@AuditTable(value = "OVPStrategia_STO")
//@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class OVPStrategia extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idovp", referencedColumnName = "id", nullable = false)
    private OVP ovp;

    @Column(name = "codstrategia", length = 100)
    private String codStrategia;

    @Column(name = "denominazionestrategia")
    private String denominazioneStrategia;

    @Column(name = "descrizionestrategia")
    private String descrizioneStrategia;

    @Column(name = "soggettoresponsabile")
    private String soggettoResponsabile;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovpStrategia", cascade = {CascadeType.PERSIST, CascadeType.MERGE},orphanRemoval = false)
    private List<OVPStrategiaIndicatore> indicatori ;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovpStrategia", cascade = {CascadeType.PERSIST, CascadeType.MERGE},orphanRemoval = false)
    private List<ObbiettivoPerformance> obbiettiviPerformance;

    @OneToMany(mappedBy = "ovpStrategia")
private List<ObiettivoPrevenzioneCorruzioneTrasparenza> obiettivoPrevenzioneCorruzioneTrasparenza;
}
