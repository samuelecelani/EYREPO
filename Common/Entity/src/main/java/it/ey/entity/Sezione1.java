package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
//import it.ey.entity.listener.Sezione1EntityListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "sezione1")
//@EntityListeners(Sezione1EntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class Sezione1 extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idStato")
    private Long  idStato;

    @Column(name = "quadroNormativo")
    private String quadroNormativo;

    @Column(name = "strutturaProgrammatica")
    private String strutturaProgrammatica;

    @Column(name = "cronoprogramma")
    private String cronoprogramma;

    @Column(name = "missione")
    private String missione;

    @OneToMany(mappedBy = "sezione1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AreaOrganizzativa> areeOrganizzative;

    @OneToMany(mappedBy = "sezione1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrioritaPolitica> prioritaPolitiche;

    @OneToMany(mappedBy = "sezione1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrincipioGuida> principiGuida;

    @OneToMany(mappedBy = "sezione1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrganoPolitico> organiPolitici;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @OneToMany(mappedBy = "sezione1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IntegrationTeam> integrationTeams;

    @OneToOne(mappedBy = "sezione1", cascade = CascadeType.ALL, orphanRemoval = true)
    private Anagrafica anagrafica;



}

