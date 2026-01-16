package it.ey.entity;



import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "integrationteam")

@Audited
@AuditTable(value = "IntegrationTeam_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsezione1", nullable = false)
    private Sezione1 sezione1;

    @Column(name = "membro")
    private String membro;

    @Column(name = "ruolo")
    private String ruolo;
}

