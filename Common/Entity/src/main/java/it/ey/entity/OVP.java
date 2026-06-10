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
@Table(name = "OVP")
//@AuditTable(value = "OVP_STO")
//@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class OVP extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idsezione21", referencedColumnName = "id", nullable = false)
    private Sezione21 sezione21;

    @Column(name = "codice", length = 100)
    private String codice;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "contesto", columnDefinition = "TEXT")
    private String contesto;

    @Column(name = "ambito", columnDefinition = "TEXT")
    private String ambito;

    @Column(name = "responsabilepolitico", length = 255)
    private String responsabilePolitico;

    @Column(name = "responsabileamministrativo", length = 255)
    private String responsabileAmministrativo;

    @Column(name = "denominazione", length = 255)
    private String denominazione;

    @Column(name = "valoreindice")
    private Long valoreIndice;

    @Column(name = "descrizioneindice", columnDefinition = "TEXT")
    private String descrizioneIndice;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovp", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<OVPAreaOrganizzativa> areeOrganizzative ;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovp", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<OVPPrioritaPolitica> prioritaPolitiche;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovp", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<OVPStakeHolder> stakeholders ;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovp", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<OVPStrategia> ovpStrategias ;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovp", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<ObbiettivoPerformance> obbiettiviPerformance;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "ovp", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<OVPRisorsaFinanziaria> risorseFinanziarie;

    @OneToMany(mappedBy = "ovp", cascade = CascadeType.REMOVE)
    private List<ObiettivoPrevenzioneCorruzioneTrasparenza> obiettivoPrevenzioneCorruzioneTrasparenza;
}
