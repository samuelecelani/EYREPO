package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "StrutturaPiao", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrutturaPiao extends CampiTecnici {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "IdParent")
    private Long idParent;

    @Column(name = "numeroSezione", length = 50)
    private String numeroSezione;

    @Column(name = "testo")
    private String testo;



}

