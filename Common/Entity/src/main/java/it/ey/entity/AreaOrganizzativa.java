package it.ey.entity;


import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.util.List;

@Entity
@Table(name = "AreaOrganizzativa")
@Audited
@AuditTable(value = "AreaOrganizzativa_STO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "areaOrganizzativa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OVPAreaOrganizzativa> ovpAreaOrganizzativa ;

}

