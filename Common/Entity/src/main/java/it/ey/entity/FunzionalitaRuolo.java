package it.ey.entity;
import it.ey.dto.FunzionalitaRuoloDTO;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Funzionalita_Ruolo")
public class FunzionalitaRuolo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relazione con Funzionalita
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdFunzionalita", nullable = false)
    private Funzionalita funzionalita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRuolo", nullable = false)
    private Ruolo ruolo;

    public FunzionalitaRuolo(FunzionalitaRuoloDTO funzionalitaRuolo) {
        this.ruolo = new Ruolo(funzionalitaRuolo.getRuolo());
    }


}