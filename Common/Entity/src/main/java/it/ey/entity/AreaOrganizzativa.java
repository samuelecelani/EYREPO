package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.util.List;

@Entity
@Table(name = "AreaOrganizzativa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class AreaOrganizzativa extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idsezione1", nullable = false)
    private Sezione1 sezione1;

    @Column(name = "nomearea")
    private String nomeArea;

    @Column(name = "descrizionearea", columnDefinition = "TEXT")
    private String descrizioneArea;

    @Filter(name = "activeFilter", condition = "X_ACTIVE = true")
    @OneToMany(mappedBy = "areaOrganizzativa", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<OVPAreaOrganizzativa> ovpAreaOrganizzativa ;

}

