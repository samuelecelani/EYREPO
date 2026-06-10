package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.enums.StatoAvviso;
import it.ey.enums.TipologiaContenuto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "Avviso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Avviso extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipologiaContenuto")
    @Enumerated(EnumType.STRING)
    private TipologiaContenuto tipologiaContenuto;

    @Column(name = "dataPubblicazione")
    private LocalDate dataPubblicazione;

    @Column(name = "oggetto")
    private String oggetto;

    @Column(name = "tipologiaAmministrazione")
    private String tipologiaAmministrazione;

    @Column(name = "amministrazione")
    private String amministrazione;

    @Column(name = "messaggio", length = 2000)
    private String messaggio;

    @Column(name = "stato")
    @Enumerated(EnumType.STRING)
    private StatoAvviso stato;
}
