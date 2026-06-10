package it.ey.sync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "allegati_piao")
public class AllegatoPiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_piao", referencedColumnName = "id")
    private DocumentoPiao documentoPiao;

    @Column(name = "nome_file", nullable = false)
    private String nomeFile;

    @Column(name = "s3_key")
    private String s3_key;
}

