package it.ey.entity;

import it.ey.dto.StakeHolderDTO;
import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.entity.listener.PiaoEntityListener;
import it.ey.enums.Tipologia;
import it.ey.enums.TipologiaOnline;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Piao")
@EntityListeners(PiaoEntityListener.class)
@SuperBuilder(toBuilder = true)

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

    @Column(name = "DataApprovazione")
    private LocalDate dataApprovazione;

    @Column(name = "Url", length = 255)
    private String url;

//    @OneToOne(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Sezione1 sezione1;
//
//    @OneToOne(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Sezione21 sezione21;
//
//    @OneToOne(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Sezione22 sezione22;
//    @OneToOne(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Sezione23 sezione23;

    @OneToMany(mappedBy = "piao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StakeHolder> stakeHolders;



}
