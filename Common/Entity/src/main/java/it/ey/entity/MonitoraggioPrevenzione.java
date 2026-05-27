package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "MonitoraggioPrevenzione")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class MonitoraggioPrevenzione extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdMisuraPrevenzioneEventoRischio", nullable = false)
    private MisuraPrevenzioneEventoRischio misuraPrevenzioneEventoRischio;

    @Column(name = "Tipologia")
    private String tipologia;

    @Column(name = "Descrizione")
    private String descrizione;

    @Column(name = "Responsabile")
    private String responsabile;

    @Column(name = "Tempistiche")
    private String tempistiche;
}
