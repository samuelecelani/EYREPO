package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "ObiettivoPrevenzioneCorruzioneTrasparenza")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ObiettivoPrevenzioneCorruzioneTrasparenza extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Relazione con Sezione23
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione23", nullable = false)
    private Sezione23 sezione23;

     //Relazioni
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdOVP")
    private OVP ovp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdStrategiaOVP")
    private OVPStrategia ovpStrategia;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdObbiettivoPerformance")
    private ObbiettivoPerformance obbiettivoPerformance;

    @Column(name = "codice", length = 50)
    private String codice;

    @Column(name = "denominazione", length = 255)
    private String denominazione;

    @Column(name = "descrizione", length = 255)
    private String descrizione;


    @OneToMany(mappedBy = "obiettivoPrevenzioneCorruzioneTrasparenza",cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},orphanRemoval = true)
    private List<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori> indicatori;

}
