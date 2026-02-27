package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "OVPStrategiaIndicatore")
//AuditTable(value = "OVPStrategiaIndicatore_STO")
//@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class OVPStrategiaIndicatore extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idovpstrategia", referencedColumnName = "id", nullable = false)
    private OVPStrategia ovpStrategia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idindicatore", referencedColumnName = "id", nullable = false)
    private Indicatore indicatore;


}
