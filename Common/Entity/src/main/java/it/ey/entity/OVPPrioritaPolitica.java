package it.ey.entity;



import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;


@Entity
@Table(
        name = "OVPPrioritaPolitica",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ovp_prioritapolitica",
                        columnNames = {"IdOVP", "IdPrioritaPolitica"}
                )
        }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@SuperBuilder(toBuilder = true)

public class OVPPrioritaPolitica extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idovp", referencedColumnName = "id", nullable = false)
    private OVP ovp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idprioritapolitica", referencedColumnName = "id", nullable = false)
    private PrioritaPolitica prioritaPolitica;

}
