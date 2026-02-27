package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "stakeholder")

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class StakeHolder extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idpiao", nullable = false) // nome colonna FK
    private Piao piao;

    @Column(name = "nomestakeholder")
    private String nomeStakeHolder;

    @Column(name = "relazionepa", columnDefinition = "TEXT")
    private String relazionePA;

    @OneToMany(mappedBy = "stakeholder", cascade = CascadeType.REMOVE)
    private List<OVPStakeHolder> ovpLinks;

    @OneToMany(mappedBy = "stakeholder", cascade = CascadeType.REMOVE)
    private List<ObiettivoPerformanceStakeHolder> obiettiviPerformance;

    @OneToMany(mappedBy = "stakeholder", cascade = CascadeType.REMOVE)
    private List<MisuraPrevenzioneStakeholder> misurePrevenzione;


}

