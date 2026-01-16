package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "StoricoStatoSezione", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

}
