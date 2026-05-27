package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.enums.TipologiaAdempimento;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "adempimentinormativi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class AdempimentiNormativi extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSezione23", nullable = false)
    private Sezione23 sezione23;

    @Column(name = "Normativa", nullable = false)
    private String normativa;

    @Column(name = "Azione", nullable = false)
    private String azione;
}
