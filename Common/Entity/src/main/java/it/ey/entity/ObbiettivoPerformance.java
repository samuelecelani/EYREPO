package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.enums.TipologiaObbiettivo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.NotAudited;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="ObiettivoPerformance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class ObbiettivoPerformance extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "IdObiettivoPeformance")
    private Long idObiettivoPeformance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione22", nullable = false)
    private Sezione22 sezione22;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdOVP")
    private OVP ovp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdStrategiaOVP")
    private OVPStrategia ovpStrategia;

    @Column(name = "codice", nullable = false, length = 100)
    private String codice;

    @Enumerated(EnumType.STRING)
    @Column(name = "CodTipologiaFK", nullable = false, length = 40)
    private TipologiaObbiettivo tipologia;

    @Column(name = "denominazione", nullable = false, length = 255)
    private String denominazione;

    @Column (name = "responsabileAmministrativo", nullable = false, length = 255)
    private String responsabileAmministrativo;

    @Column (name = "RisorseUmane", length = 255)
    private String risorseUmane;

    @Column (name = "RisorseEconomicaFinanziaria", length = 255)
    private String risorseEconomicaFinanziaria;

    @Column (name = "RisorseStrumentali", length = 255)
    private String risorseStrumentali;

    @Column (name = "TipologiaRisorsa", length = 255)
    private String tipologiaRisorsa;

    @OneToMany(mappedBy = "obbiettivoPerformance", cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REMOVE}, orphanRemoval = true)
    private List<ObiettivoPerformanceStakeHolder> stakeholders;

    @OneToMany(mappedBy = "obbiettivoPerformance", cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REMOVE}, orphanRemoval = true)
    private List<ObiettivoPerformanceIndicatore> indicatori;
    }
