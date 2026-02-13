package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "EventoRischio")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class EventoRischio extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdAttivitaSensibile")
    private AttivitaSensibile attivitaSensibile;

    @Column(name = "Denominazione")
    private String denominazione;

    @Column(name = "Probabilita")
    private Long probabilita;

    @Column(name = "Impatto")
    private Long impatto;

    @Column(name = "Controlli")
    private String controlli;

    @Column(name = "Valutazione")
    private Long valutazione;

//TODO capire col figma che tipo di relazione abbiamo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdLivelloRischio")
    private LivelloRischio livelloRischio;

    @OneToMany(mappedBy = "eventoRischio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MisuraPrevenzioneEventoRischio> misure;


}
