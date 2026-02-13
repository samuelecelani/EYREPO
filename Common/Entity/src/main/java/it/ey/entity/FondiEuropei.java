package it.ey.entity;

import it.ey.dto.CampiTecniciDTO;
import it.ey.entity.campiTecnici.CampiTecnici;
import lombok.*;


import jakarta.persistence.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "FondiEuropei")
//@Audited
//@AuditTable(value = "FondiEuropei_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione21", nullable = false, foreignKey = @ForeignKey(name = "fk_fondieuropei_sezione21"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Sezione21 sezione21;

}
