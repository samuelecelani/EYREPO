package it.ey.entity;

import it.ey.dto.StakeHolderDTO;
import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.entity.listener.PiaoEntityListener;
import it.ey.enums.Tipologia;
import it.ey.enums.TipologiaOnline;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Piao")
@EntityListeners(PiaoEntityListener.class)
@Audited
@AuditTable(value = "Piao_STO")
public class Piao extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codPAFK", nullable = false)
    private String codPAFK;

    @Column(name = "Denominazione", nullable = false)
    private String denominazione;

    @Column(name = "Versione", length = 50)
    private String versione;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipologia", length = 50)
    private Tipologia tipologia;

    @Enumerated(EnumType.STRING)
    @Column(name = "TipologiaOnline", length = 50)
    private TipologiaOnline tipologiaOnline;

    @Column(name = "DataScadenza")
    private LocalDate dataScadenza;

    @Column(name = "IdStato")
    private Long  idStato;

    @OneToOne(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Sezione1 sezione1;
    @OneToOne(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Sezione21 sezione21;
    @OneToMany(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StakeHolder> stakeHolders;



}
