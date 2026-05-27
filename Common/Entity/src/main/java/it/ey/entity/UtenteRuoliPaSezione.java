package it.ey.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utenteRuoliPaSezione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtenteRuoliPaSezione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "idUtenteRuoliPa", nullable = false)
    private String externalUserId;

    @Column(name = "idAmministrazione")
    private String idAmministrazione;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idStruttura", nullable = false)
    private StrutturaPiao strutturaPiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idRuolo", nullable = false)
    private Ruolo ruolo;
}

