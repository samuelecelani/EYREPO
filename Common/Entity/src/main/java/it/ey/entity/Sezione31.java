package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.util.List;

@Entity
@Table(name = "Sezione31")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione31 extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @Column(name = "idStato")
    private Long idStato;

    @Column(name = "strutturaorganizzativaap")
    private String strutturaOrganizzativaAP;

    @Column(name = "ampiezzaorganica")
    private String ampiezzaOrganica;

    @Column(name = "incarichidirigenziali")
    private String incarichiDirigenziali;

    @Column(name = "profiliprofessionali")
    private String profiliProfessionali;

    @Column(name = "lineeorganizzazione")
    private String lineeOrganizzazione;

    @OneToMany(mappedBy = "sezione31", cascade = {CascadeType.PERSIST,CascadeType.MERGE}, orphanRemoval = false)
    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    private List<AmpiezzaOrganizzativa> ampiezzaOrganizzative;

    @Column(name = "GraficoMinerva")
    private Boolean graficoMinerva;
}
