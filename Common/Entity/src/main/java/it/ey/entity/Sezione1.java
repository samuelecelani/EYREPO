package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
//import it.ey.entity.listener.Sezione1EntityListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

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

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "sezione1",  cascade = {CascadeType.PERSIST,CascadeType.MERGE}, orphanRemoval = false)
    private List<AreaOrganizzativa> areeOrganizzative;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "sezione1", cascade = {CascadeType.PERSIST,CascadeType.MERGE}, orphanRemoval = false)
    private List<PrioritaPolitica> prioritaPolitiche;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "sezione1", cascade =  {CascadeType.PERSIST,CascadeType.MERGE}, orphanRemoval = false)
    private List<PrincipioGuida> principiGuida;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "sezione1", cascade = {CascadeType.PERSIST,CascadeType.MERGE}, orphanRemoval = false)
    private List<OrganoPolitico> organiPolitici;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPiao", referencedColumnName = "id", nullable = false)
    private Piao piao;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "sezione1", cascade = {CascadeType.PERSIST,CascadeType.MERGE}, orphanRemoval = false)
    private List<IntegrationTeam> integrationTeams;


}

