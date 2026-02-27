package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "allegato")
@SuperBuilder(toBuilder = true)

public class Allegato extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identitafk", nullable = false)
    private Long idEntitaFK;

    @Column(name = "coddocumento", nullable = false)
    private String codDocumento; // URL S3

    @Enumerated(EnumType.STRING)
    @Column(name = "codtipologiafk", nullable = false)
    private CodTipologia codTipologiaFK;

    @Enumerated(EnumType.STRING)
    @Column(name = "codtipologiaallegato", nullable = false)
    private CodTipologiaAllegato codTipologiaAllegato;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "sizeAllegato")
    private String sizeAllegato;

}