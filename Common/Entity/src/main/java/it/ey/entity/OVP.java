package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Table(name = "OVP")
@AuditTable(value = "OVP_STO")
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OVP extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idsezione21", referencedColumnName = "id", nullable = false)
    private Sezione21 sezione21;

    @Column(name = "codice", length = 100)
    private String codice;

    @Column(name = "descrizione", columnDefinition = "TEXT")
    private String descrizione;

    @Column(name = "contesto", columnDefinition = "TEXT")
    private String contesto;

    @Column(name = "ambito", columnDefinition = "TEXT")
    private String ambito;

    @Column(name = "responsabilepolitico", length = 255)
    private String responsabilePolitico;

    @Column(name = "responsabileamministrativo", length = 255)
    private String responsabileAmministrativo;

    @Column(name = "valoreindice")
    private Long valoreIndice;

    @Column(name = "descrizioneindice", columnDefinition = "TEXT")
    private String descrizioneIndice;

    @OneToMany(mappedBy = "ovp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OVPAreaOrganizzativa> areeOrganizzative ;

    @OneToMany(mappedBy = "ovp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OVPPrioritaPolitica> prioritaPolitiche ;

    @OneToMany(mappedBy = "ovp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OVPStakeHolder> stakeholders ;

    @OneToMany(mappedBy = "ovp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OVPStrategia> ovpStrategia ;

}
