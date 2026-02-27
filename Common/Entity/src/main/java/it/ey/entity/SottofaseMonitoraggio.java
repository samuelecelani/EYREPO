package it.ey.entity;

import it.ey.entity.Sezione4;
import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "SottofaseMonitoraggio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SottofaseMonitoraggio extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSezione4", nullable = false)
    private Sezione4 sezione4;

    @Column(name = "Denominazione", nullable = false, length = 255)
    private String denominazione;

    @Column(name = "Descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "DataInizio", nullable = false)
    private LocalDate dataInizio;

    @Column(name = "DataFine", nullable = false)
    private LocalDate dataFine;

    @Column(name = "Strumenti", length = 255)
    private String strumenti;

    @Column(name = "FonteDato", length = 255)
    private String fonteDato;


    @OneToMany(mappedBy = "sottofaseMonitoraggio", cascade = CascadeType.REMOVE, orphanRemoval = false)
    private List<Milestone> milestone;
}
 