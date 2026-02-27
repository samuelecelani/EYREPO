package it.ey.entity;

import it.ey.entity.tipologica.Tipologica;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dimensioneindicatore")

public class DimensioneIndicatore extends Tipologica {

    @Column(name = "codtipologiafk", nullable = false)
    private String codTipologiaFK;
}
