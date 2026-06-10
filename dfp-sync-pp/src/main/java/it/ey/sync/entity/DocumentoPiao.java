package it.ey.sync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documenti_piao")
public class DocumentoPiao {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "codice_piao", length = 50, nullable = false)
    private String codicePiao;

    @Column(name = "full_name")
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codice_ipa_rif", referencedColumnName = "codice_ipa")
    private Amministrazione amministrazione;

    @Column(name = "versione")
    private Integer versione;

    @Column(name = "data_approvazione")
    private LocalDate dataApprovazione;

    @Column(name = "data_pubblicazione")
    private LocalDate dataPubblicazione;

    @Column(name = "link_esterno")
    private String linkEsterno;

    @OneToMany(mappedBy = "documentoPiao", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<AllegatoPiao> allegati;
}

