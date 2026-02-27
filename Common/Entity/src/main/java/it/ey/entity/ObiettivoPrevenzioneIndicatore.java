package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ObiettivoPrevenzioneIndicatore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class ObiettivoPrevenzioneIndicatore extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relazione verso ObiettivoPrevenzione
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdObiettivoPrevenzione", nullable = false)
    private ObiettivoPrevenzione obiettivoPrevenzione;

    // Relazione verso Indicatore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdIndicatore", nullable = false)
    private Indicatore indicatore;

}
