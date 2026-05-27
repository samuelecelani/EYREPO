package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "MisuraPrevenzioneIndicatore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class MisuraPrevenzioneIndicatore extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdMisuraPrevenzione", nullable = false)
    private MisuraPrevenzione misuraPrevenzione;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdIndicatore", nullable = false)
    private Indicatore indicatore;
}
