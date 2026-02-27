package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "StoricoStatoSezione", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class StoricoStatoSezione extends CampiTecnici{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdStato", referencedColumnName = "id")
    private StatoSezione statoSezione;

    @Column(name = "IdEntitaFK")
    private Long idEntitaFK;

    @Column(name = "CodTipologiaFK")
    private String  codTipologiaFK;

    @Column(name = "testo", nullable = false)
    private   String testo;

    @Column(name = "rifiutato", columnDefinition = "bool default false")
    @Builder.Default
    private Boolean rifiutato = false;

    @Column(name = "revocato", columnDefinition = "bool default false")
    @Builder.Default
    private Boolean revocato = false;

}
