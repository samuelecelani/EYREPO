package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "principioguida")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PrincipioGuida extends CampiTecnici {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "idsezione1", nullable = false)
        private Sezione1 sezione1;

        @Column(name = "nomeprincipioguida")
        private String nomePrincipioGuida;

        @Column(name = "descrizioneprincipioguida", columnDefinition = "TEXT")
        private String descrizionePrincipioGuida;
    }

