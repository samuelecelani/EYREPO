package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdObiettivoPrevenzioneCorruzioneTrasparenza", nullable = false)
    private ObiettivoPrevenzioneCorruzioneTrasparenza  obiettivoPrevenzioneCorruzioneTrasparenza;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdIndicatore", nullable = false)
    private Indicatore indicatore;
}
