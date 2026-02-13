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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtenteRuoliPa", nullable = false)
    private UtenteRuoloPa utenteRuoloPa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idStruttura", nullable = false)
    private StrutturaPiao strutturaPiao;
}

