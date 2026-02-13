package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Table(name = "Sezione22")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@SuperBuilder(toBuilder = true)

public class Sezione22 extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @Column(name = "idStato")
    private Long  idStato;

    @Column(name = "IntroPerformance")
    private String  introPerformance;

    @Column(name = "IntroObiettiviPerformance")
    private String  introObiettiviPerformance;

    @Column(name = "IntroAdempimenti")
    private String  introAdempimenti;

    @Column(name = "IntroPerformanceOrganizzativa")
    private  String  introPerformanceOrganizzativa;

    @Column(name = "DescrizioneCollegamentoPerformance")
    private  String descriptionCollegamentoPerformance;

    @Column(name = "IntroPerformanceIndividuale")
    private  String introPerformanceIndividuale;

    @OneToMany(mappedBy = "sezione22", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Fase> fase;

    @OneToMany(mappedBy = "sezione22", cascade = CascadeType.REMOVE , orphanRemoval = true)
    private List<ObbiettivoPerformance> obbiettiviPerformance;

    @OneToMany(mappedBy = "sezione22", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Adempimento> adempimenti;
}
