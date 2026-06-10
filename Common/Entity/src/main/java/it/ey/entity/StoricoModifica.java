package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "StoricoModifica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StoricoModifica extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPiao", referencedColumnName = "id")
    private Piao piao;

    @Column(name = "IdSezione")
    private Long idSezione;

    @Column(name = "CodTipologiaFK")
    private String codTipologiaFK;

    @Column(name = "NomeCognome")
    private String nomeCognome;

    @Column(name = "Profilo")
    private String profilo;

    @Column(name = "DataModifica")
    private LocalDate dataModifica;

    @Column(name = "Sezione")
    private String sezione;

    @Column(name = "TestoSezione", columnDefinition = "TEXT")
    private String testoSezione;

    @Column(name = "CampiModificati", columnDefinition = "TEXT")
    private String campiModificati;
}
