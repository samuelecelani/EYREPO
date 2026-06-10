package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ObiettivoPerformanceIndicatore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class ObiettivoPerformanceIndicatore extends CampiTecnici {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "IdObiettivoPerformance", nullable = false)
        private ObbiettivoPerformance obbiettivoPerformance;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "IdIndicatore", nullable = false)
        private Indicatore indicatore;

    }

