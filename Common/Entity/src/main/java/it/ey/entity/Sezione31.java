package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    private String incarichidirigenziali;

    @Column(name = "profiliprofessionali")
    private String profiliProfessionali;

    @Column(name = "lineeorganizzazione")
    private String lineeOrganizzazione;
}
