package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.util.List;

@Entity
@Table(name = "Sezione4")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione4 extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPiao", nullable = false)
    private Piao piao;

    @Column(name = "IdStato")
    private Long idStato;

    @Column(columnDefinition = "TEXT")
    private String descrStrumenti;

    @Column(columnDefinition = "TEXT")
    private String descrModalitaRilevazione;

    @Column(columnDefinition = "TEXT")
    private String intro;

    @Column(columnDefinition = "TEXT")
    private String intro21;

    @Column(columnDefinition = "TEXT")
    private String intro22;

    @Column(columnDefinition = "TEXT")
    private String descr22;

    @Column(columnDefinition = "TEXT")
    private String descr23;

    @Column(columnDefinition = "TEXT")
    private String descr31;

    @Column(columnDefinition = "TEXT")
    private String descr32;

    @Column(columnDefinition = "TEXT")
    private String descr331;

    @Column(columnDefinition = "TEXT")
    private String descr332;

    @Column(columnDefinition = "TEXT")
    private String descrMonitoraggio;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "sezione4", cascade = CascadeType.REMOVE, orphanRemoval = false)
    private List<SottofaseMonitoraggio> sottofaseMonitoraggio;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "sezione4", cascade = CascadeType.REMOVE, orphanRemoval = false)
    private List<CategoriaObiettivi> categoriaObiettivi;

}
