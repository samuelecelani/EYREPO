package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(
        name = "OVPAreaOrganizzativa",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ovp_areaorganizzativa",
                        columnNames = {"IdOVP", "IdAreaOrganizzativa"}
                )
        }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class OVPAreaOrganizzativa  extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idovp", referencedColumnName = "id", nullable = false)
    private OVP ovp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idareaorganizzativa", referencedColumnName = "id", nullable = false)
    private AreaOrganizzativa areaOrganizzativa;

}