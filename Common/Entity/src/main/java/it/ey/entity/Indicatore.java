package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.NotAudited;

import java.util.List;


@Entity
@Table(name = "indicatore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

//@Audited
//@AuditTable(value = "Indicatore_STO")
public class Indicatore extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @Column(name = "identitafk")
    private Long idEntitaFK;

    @Column(name = "codtipologiafk")
    private String codTipologiaFK;

    @Column(name = "denominazione")
    private String denominazione;

    @Column(name = "idsubDimensionefk")
    private Long idSubDimensioneFK;

    @Column(name = "iddimensionefk")
    private Long idDimensioneFK;

    @Column(name = "unitaMisura")
    private String unitaMisura;

    @Column(name = "formula")
    private String formula;

    @Column(name = "peso")
    private Long peso;

    @Column(name = "polarita")
    private String polarita;

    @Column(name = "baseLine")
    private Long baseLine;

    @Column(name = "consuntivo")
    private Long consuntivo;

    @Column(name = "fonteDati")
    private String fonteDati;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE ,optional = true)
    @JoinColumn(name = "IdTipAndValAnnoCorrente", foreignKey = @ForeignKey(name = "fk_IndicatoreTipologia_AnnoCorrente"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotAudited
    private TipologiaAndamentoValoreIndicatore tipAndValAnnoCorrente;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.MERGE ,optional = true)
    @JoinColumn(name = "IdTipAndValAnno1", foreignKey = @ForeignKey(name = "fk_IndicatoreTipologia_Anno1"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotAudited
    private TipologiaAndamentoValoreIndicatore tipAndValAnno1;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, optional = true)
    @JoinColumn(name = "IdTipAndValAnno2", foreignKey = @ForeignKey(name = "fk_IndicatoreTipologia_Anno2"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotAudited
    private TipologiaAndamentoValoreIndicatore tipAndValAnno2;

    @Column(name = "rilevante")
    private Boolean rilevante;

    @OneToMany(mappedBy = "indicatore")
    private List<OVPStrategiaIndicatore> ovpStrategiaIndicatore ;

    @OneToMany(mappedBy = "indicatore", cascade = CascadeType.REMOVE)
    private List<ObiettivoPerformanceIndicatore> obbiettiviPerformance;

    @OneToMany(mappedBy = "indicatore", cascade = CascadeType.REMOVE)
    private List<ObiettivoPrevenzioneIndicatore> obiettiviPrevenzioneIndicatore;

    @OneToMany(mappedBy = "indicatore", cascade = CascadeType.REMOVE)
    private List<MisuraPrevenzioneIndicatore> misure;

}

