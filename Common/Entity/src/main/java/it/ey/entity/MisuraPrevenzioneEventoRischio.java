package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "MisuraPrevenzioneEventoRischio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MisuraPrevenzioneEventoRischio extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice", nullable = false)
    private String codice;

    @Column(name = "denominazione", nullable = false)
    private String denominazione;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "responsabile", length = 255)
    private String responsabile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdObiettivoPrevenzioneCorruzioneTrasparenza")
    private ObiettivoPrevenzioneCorruzioneTrasparenza obiettivoPrevenzioneCorruzioneTrasparenza;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdEventoRischio", nullable = false)
    private EventoRischio eventoRischio;


    @OneToMany(mappedBy = "misuraPrevenzioneEventoRischio",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<MisuraPrevenzioneEventoRischioIndicatore> indicatori;


    @OneToMany(mappedBy = "misuraPrevenzioneEventoRischio",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<MisuraPrevenzioneEventoRischioStakeholder> stakeholder;

    @OneToMany(mappedBy = "misuraPrevenzioneEventoRischio",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<MonitoraggioPrevenzione> monitoraggioPrevenzione;


}
