package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import jdk.jfr.Event;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Entity
@Table(name = "AttivitaSensibile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class AttivitaSensibile extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione23", nullable = false)
    private Sezione23 sezione23;

    @Column(name = "denominazione", nullable = false, length = 255)
    private String denominazione;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "processoCollegato")
    private String processoCollegato;

    @OneToMany(mappedBy = "attivitaSensibile", cascade = CascadeType.REMOVE)
    private List<EventoRischio> eventoRischio;
}
