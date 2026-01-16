
package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(
        name = "OVPStakeHolder",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ovp_stakeholder",
                        columnNames = {"IdOVP", "IdStakeHolder"}
                )
        }
)
@Audited
@AuditTable(value = "OVPStakeHolder_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class OVPStakeHolder extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idovp", referencedColumnName = "id", nullable = false)
    private OVP ovp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idstakeholder", referencedColumnName = "id", nullable = false)
    private StakeHolder stakeHolder;

}