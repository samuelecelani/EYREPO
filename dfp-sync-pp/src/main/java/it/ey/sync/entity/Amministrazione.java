package it.ey.sync.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "amministrazioni")
public class Amministrazione {

    @Id
    @Column(name = "codice_ipa", length = 50)
    private String codiceIpa;

    @Column(name = "denominazione", nullable = false)
    private String denominazione;

    @Column(name = "tipologia", length = 50, nullable = false)
    private String tipologia;
}

