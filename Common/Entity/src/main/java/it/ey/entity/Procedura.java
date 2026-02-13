package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "Procedura")
//@Audited
//@AuditTable(value = "Procedura_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class Procedura extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdSezione21", nullable = false, foreignKey = @ForeignKey(name = "fk_procedure_sezione21"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Sezione21 sezione21;

    @Column(name = "Denominazione", length = 255)
    private String denominazione;

    @Column(name = "Descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "UnitaMisura", length = 100)
    private String unitaMisura;

    @Column(name = "Misurazione", length = 100)
    private String misurazione;

    @Column(name = "Target", length = 100)
    private String target;

    @Column(name = "UffResponsabile", length = 255)
    private String uffResponsabile;

}

