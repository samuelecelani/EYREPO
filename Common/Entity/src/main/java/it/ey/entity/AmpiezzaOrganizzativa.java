package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "AmpiezzaOrganizzativa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AmpiezzaOrganizzativa extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsezione31", nullable = false)
    private Sezione31 sezione31;

    @Column(name = "unitaorganizzativa")
    private String unitaOrganizzativa;

    @Column(name = "numrisorseumane")
    private String numRisorseUmane;
}
