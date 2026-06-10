package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import it.ey.enums.CodTipologiaCategoria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "CategoriaObiettiviTip")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CategoriaObiettiviTip extends CampiTecnici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="testo")
    private String testo;

    @Enumerated(EnumType.STRING)
    @Column(name = "CodTipologiaFk")
    private CodTipologiaCategoria codTipologiaFK;

    // la relazione deve esistere anche da questo lato
}
