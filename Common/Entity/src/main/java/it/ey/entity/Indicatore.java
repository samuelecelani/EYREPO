package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
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
@Builder
@Audited
@AuditTable(value = "Indicatore_STO")
public class Indicatore extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "denominazione")
    private String denominazione;

    @Column(name = "subDimensione")
    private String subDimensione;

    @Column(name = "dimensione")
    private String dimensione;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IdTipAndValAnnoCorrente", foreignKey = @ForeignKey(name = "fk_IndicatoreTipologia_AnnoCorrente"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotAudited
    private TipologiaAndamentoValoreIndicatore idTipAndValAnnoCorrente;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IdTipAndValAnno1", foreignKey = @ForeignKey(name = "fk_IndicatoreTipologia_Anno1"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotAudited
    private TipologiaAndamentoValoreIndicatore idTipAndValAnno1;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "IdTipAndValAnno2", foreignKey = @ForeignKey(name = "fk_IndicatoreTipologia_Anno2"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotAudited
    private TipologiaAndamentoValoreIndicatore idTipAndValAnno2;

    @Column(name = "rilevante")
    private Boolean rilevante;

    @OneToMany(mappedBy = "indicatore", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OVPStrategiaIndicatore> ovpStrategiaIndicatore ;
}
