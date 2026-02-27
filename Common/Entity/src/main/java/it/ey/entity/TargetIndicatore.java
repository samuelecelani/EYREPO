package it.ey.entity;

import it.ey.entity.tipologica.Tipologica;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "targetindicatore")
public class TargetIndicatore extends Tipologica {
}
