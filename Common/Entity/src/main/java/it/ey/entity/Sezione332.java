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
@Table(name = "Sezione332")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Sezione332 extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    // NON relazione – solo tracking per STO
    @Column(name = "IdStato")
    private Long idStato;

    @Column(name = "ContestoNormativo", nullable = false, columnDefinition = "TEXT")
    private String contestoNormativo;

    @Column(name = "DescrizioneQualitativa", nullable = false, columnDefinition = "TEXT")
    private String descrizioneQualitativa;

    @Column(name = "DescrizioneStrategia", nullable = false, columnDefinition = "TEXT")
    private String descrizioneStrategia;

    @Column(name = "DescrizioneRisorse", nullable = false, columnDefinition = "TEXT")
    private String descrizioneRisorse;

    @Column(name = "DescrizioneIncentivi", nullable = false, columnDefinition = "TEXT")
    private String descrizioneIncentivi;

    @OneToMany(mappedBy = "sezione332", cascade = CascadeType.REMOVE)
    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    private List<ObiettiviRisultatiFotografia> obiettiviRisultatiFotografia;

    @OneToMany(mappedBy = "sezione332", cascade = CascadeType.REMOVE)
    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    private List<AttivitaFormative> attivitaFormative;
}
