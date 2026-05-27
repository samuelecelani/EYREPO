package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "Sezione331")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Sezione331  extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;


    @Column(name = "Contesto", nullable = false, columnDefinition = "TEXT")
    private String contesto;

    @Column(name = "DescrizioneQualitativa", nullable = false, columnDefinition = "TEXT")
    private String descrizioneQualitativa;

    @Column(name = "StrategiaProgrammazione", nullable = false, columnDefinition = "TEXT")
    private String strategiaProgrammazione;

    @Column(name = "ObiettivoTrasformazione", nullable = false, columnDefinition = "TEXT")
    private String obiettivoTrasformazione;

    @Column(name = "Rimodulazione", nullable = false)
    private Boolean rimodulazione;

    @Column(name = "StrategiaCopertura", nullable = false, columnDefinition = "TEXT")
    private String strategiaCopertura;

    @Column(name = "DescrizioneStrategia", nullable = false, columnDefinition = "TEXT")
    private String descrizioneStrategia;

    @Column(name = "StimaEvoluzione", nullable = false, columnDefinition = "TEXT")
    private String stimaEvoluzione;

    // NON relazione – solo tracking per STO
    @Column(name = "IdStato")
    private Long idStato;


}

