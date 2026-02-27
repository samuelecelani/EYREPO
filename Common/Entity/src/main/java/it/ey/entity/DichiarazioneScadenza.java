package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name="dichiarazionescadenza")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DichiarazioneScadenza extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "annoriferimento", nullable = false)
    private Long annoRiferimento;

    @Column(name = "datapubblicazione", nullable = false)
    private LocalDate dataPubblicazione;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idmotivazionedichiarazione", nullable = false)
    private MotivazioneDichiarazione motivazioneDichiarazione;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "responsabile", nullable = false)
    private String responsabile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPiao", nullable = false)
    private Piao piao;
}
