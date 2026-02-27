package it.ey.entity;

import it.ey.dto.CampiTecniciDTO;
import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Table(name = "Sezione21")
//@Audited
//@AuditTable(value = "Sezione21_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class Sezione21 extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @Column(name = "idStato")
    private Long  idStato;

    @Column(name = "ContestoInt")
    private String contestoInt;

    @Column(name = "ContestoExt")
    private String contestoExt;

    @Column(name = "introrisorsefinanziarie", columnDefinition = "TEXT")
    private String introRisorseFinanziarie;

    @Column(name = "introfondieuropei", columnDefinition = "TEXT")
    private String introFondiEuropei;

    @Column(name = "introprocedure", columnDefinition = "TEXT")
    private String introProcedure;

    @Column(name = "DescrizioneValorePubblico")
    private String descrizioneValorePubblico;

    @Column(name = "DescrizioneAccessiDigitale")
    private String descrizioneAccessiDigitale;

    @Column(name = "DescrizioneAccessiFisica")
    private String descrizioneAccessiFisica;

    @Column(name = "DescrizioneSemplificazione")
    private String descrizioneSemplificazione;

    @Column(name = "DescrizionePariOpportunita")
    private String descrizionePariOpportunita;

    @OneToMany(mappedBy = "sezione21", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<FondiEuropei> fondiEuropei;
    
    @OneToMany(mappedBy = "sezione21", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<OVP> ovpList;

    @OneToMany(mappedBy = "sezione21", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Procedura> procedure ;



}

