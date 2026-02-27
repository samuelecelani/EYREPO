package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.enums.CodTipologiaCategoria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "CategoriaObiettivi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class CategoriaObiettivi extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSezione4", nullable = false)
    private Sezione4 sezione4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSottofase")
    private SottofaseMonitoraggio sottofase;

    @Column(name = "IdCategoriaObbiettivi")
    private Long idCategoriaObbiettivi;

    @Enumerated(EnumType.STRING)
    @Column(name = "CodTipologiaFk")
    private CodTipologiaCategoria codTipologiaFk;
}
