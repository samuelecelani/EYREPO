package it.ey.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organopolitico")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganoPolitico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsezione1", nullable = false)
    private Sezione1 sezione1;

    @Column(name = "organo")
    private String organo;

    @Column(name = "ruolo", columnDefinition = "TEXT")
    private String ruolo;
}

