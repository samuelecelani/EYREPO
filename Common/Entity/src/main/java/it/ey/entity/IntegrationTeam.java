package it.ey.entity;



import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "integrationteam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

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

