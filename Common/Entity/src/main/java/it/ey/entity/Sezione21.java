package it.ey.entity;

import it.ey.dto.CampiTecniciDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Table(name = "Sezione21")
@Audited
@AuditTable(value = "Sezione21_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sezione21 extends CampiTecniciDTO {

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
    private List<FondiEuropei> fondiEuropei;
    
    @OneToMany(mappedBy = "sezione21", cascade = CascadeType.PERSIST /* o NONE */, orphanRemoval = false)
    private List<OVP> ovpList;

    @OneToMany(mappedBy = "sezione21", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE) // opzionale; utile lato Hibernate quando si rimuove Sezione21
    private List<Procedura> procedure ;



}

