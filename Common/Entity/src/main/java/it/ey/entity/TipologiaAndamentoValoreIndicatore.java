package it.ey.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "TipologiaAndamentoValoreIndicatore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipologiaAndamentoValoreIndicatore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name= "idtargetfk")
    private Long idTargetFK;
    @Column(name= "Valore")
    private String valore;


}
