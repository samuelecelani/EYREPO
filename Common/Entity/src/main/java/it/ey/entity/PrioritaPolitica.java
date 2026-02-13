package it.ey.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Entity
@Table(name = "prioritapolitica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrioritaPolitica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsezione1", nullable = false)
    private Sezione1 sezione1;

    @Column(name = "nomeprioritapolitica")
    private String nomePrioritaPolitica;

    @Column(name = "descrizioneprioritapolitica", columnDefinition = "TEXT")
    private String descrizionePrioritaPolitica;

    @OneToMany(mappedBy = "prioritaPolitica", cascade = CascadeType.REMOVE)
    private List<OVPPrioritaPolitica> ovpPrioritaPoliticas ;
}
