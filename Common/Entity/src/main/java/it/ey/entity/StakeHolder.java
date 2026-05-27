package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

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

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "stakeholder", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<OVPStakeHolder> ovpLinks;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "stakeholder", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<ObiettivoPerformanceStakeHolder> obiettiviPerformance;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "stakeholder", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<MisuraPrevenzioneStakeholder> misurePrevenzione;

    @OneToMany(mappedBy = "stakeholder", cascade = CascadeType.REMOVE)
    private List<MisuraPrevenzioneEventoRischioStakeholder> misurePrevenzioneEventoRischio;


}

