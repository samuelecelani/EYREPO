package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "RichiestaApprovazione")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RichiestaApprovazione extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;




     @OneToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false, unique = true)
      private Piao piao;

    @Column(name = "Mail", nullable = false, length = 255)
    private String mail;

    @Column(name = "Oggetto", nullable = false, columnDefinition = "TEXT")
    private String oggetto;

    @Column(name = "Testo", nullable = false, columnDefinition = "TEXT")
    private String testo;
}