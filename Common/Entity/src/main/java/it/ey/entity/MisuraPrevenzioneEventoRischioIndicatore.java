package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "MisuraPrevenzioneEventoRischioIndicatore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MisuraPrevenzioneEventoRischioIndicatore  extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELAZIONE VERSO MISURA
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdMisuraPrevenzioneEventoRischio", nullable = false)
    private MisuraPrevenzioneEventoRischio misuraPrevenzioneEventoRischio;

    // RELAZIONE VERSO INDICATORE
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdIndicatore", nullable = false)
    private Indicatore indicatore;
}
