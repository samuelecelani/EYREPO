package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "MisuraPrevenzione")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class MisuraPrevenzione  extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione23", nullable = false)
    private Sezione23 sezione23;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdObiettivoPrevenzione", nullable = false)
    private ObiettivoPrevenzione obiettivoPrevenzione;

    @Column(name = "denominazione", nullable = false, length = 255)
    private String denominazione;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "codice")
    private String codice;

    @Column(name = "responsabileMisura", nullable = false, length = 255)
    private String responsabileMisura;

    @OneToMany(mappedBy = "misuraPrevenzione", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MisuraPrevenzioneIndicatore> indicatori;

    @OneToMany(mappedBy = "misuraPrevenzione", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MisuraPrevenzioneStakeholder> stakeholder;


}
