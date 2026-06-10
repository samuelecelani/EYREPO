package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.enums.TipologiaAdempimento;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "Adempimento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class Adempimento extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSezione22", nullable = false)
    private Sezione22 sezione22;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipologia", nullable = false)
    private TipologiaAdempimento tipologia;

    @Column(name = "Denominazione", nullable = false)
    private String denominazione;
}
