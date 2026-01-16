package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Table(name = "stakeholder")
@Audited
@AuditTable(value = "StakeHolder_STO")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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

    @OneToMany(mappedBy = "stakeHolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OVPStakeHolder> ovpLinks;



}

