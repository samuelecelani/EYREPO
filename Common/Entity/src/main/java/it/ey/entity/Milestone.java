package it.ey.entity;

import it.ey.entity.campiTecnici.CampiTecnici;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "milestone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Milestone extends CampiTecnici
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdSottofaseMonitoraggio")
    private SottofaseMonitoraggio sottofaseMonitoraggio;

    @Column(name = "Descrizione")
    private String descrizione;

    @Column(name = "Data")
    private LocalDate data;

    @Column(name = "IsPromemoria")
    private Boolean isPromemoria;

    @Column(name = "DataPromemoria")
    private LocalDate dataPromemoria;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdPromemoria", referencedColumnName = "id")
    private Promemoria promemoria;
}
