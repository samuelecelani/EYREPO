package it.ey.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "principioguida")
@Audited
@AuditTable(value = "PrincipioGuida_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrincipioGuida {

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

