package it.ey.entity;

import it.ey.dto.CampiTecniciDTO;
import it.ey.entity.campiTecnici.CampiTecnici;
import lombok.*;


import jakarta.persistence.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "FondiEuropei")
@Audited
@AuditTable(value = "FondiEuropei_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FondiEuropei extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ProgettoFinanziato", length = 255)
    private String progettoFinanziato;

    @Column(name = "Descrizione")
    private String descrizione;

    @Column(name = "FondiStanziati")
    private Double fondiStanziati;
    // Relazione con Sezione21
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSezione21", nullable = false)
    private Sezione21 sezione21;

}
