package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "ObiettivoPrevenzione")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class ObiettivoPrevenzione extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione23", nullable = false)
    private Sezione23 sezione23;

    @Column(name = "denominazione", length = 255)
    private String denominazione;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "codice")
    private String codice;

    @OneToMany(mappedBy = "obiettivoPrevenzione", cascade = {CascadeType.PERSIST, CascadeType.MERGE,CascadeType.REMOVE}, orphanRemoval = true)
    private List<ObiettivoPrevenzioneIndicatore> indicatori;

    @OneToMany(mappedBy = "obiettivoPrevenzione", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<MisuraPrevenzione> misurePrevenzione;


}
