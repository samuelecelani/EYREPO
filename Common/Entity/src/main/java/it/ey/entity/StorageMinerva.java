package it.ey.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mappa la tabella public.storageminerva:
 * <pre>
 *  id           bigserial PK
 *  identitafk   int8 NOT NULL
 *  codtipologiafk varchar(100) NOT NULL
 *  valore       text NOT NULL
 *  codiceipa    varchar(50) NULL
 * </pre>
 */
@Entity
@Table(name = "storageminerva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class StorageMinerva
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identitafk", nullable = false)
    private Long identitafk;

    @Column(name = "codtipologiafk", nullable = false, length = 100)
    private String codtipologiafk;

    @Column(name = "valore", nullable = false, columnDefinition = "text")
    private String valore;

    @Column(name = "codiceipa", length = 50)
    private String codiceipa;
}

