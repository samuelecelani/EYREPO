package it.ey.piao.api.repository;

import it.ey.entity.StorageMinerva;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IStorageMinervaRepository extends BaseRepository<StorageMinerva, Long>
{
    /**
     * Trova il record per identitafk + codiceipa + codtipologiafk (chiave logica di upsert).
     */
    @Query("""
            SELECT s
            FROM StorageMinerva s
            WHERE s.identitafk = :identitafk
              AND s.codtipologiafk = :codtipologiafk
              AND ( (:codiceipa IS NULL AND s.codiceipa IS NULL)
                    OR s.codiceipa = :codiceipa )
    """)
    Optional<StorageMinerva> findByIdentitafkAndCodiceipaAndCodtipologiafk(
        @Param("identitafk") Long identitafk,
        @Param("codiceipa") String codiceipa,
        @Param("codtipologiafk") String codtipologiafk);

    /** Tutti i record con quell'identitafk (utile per recuperare tutte le tabelle di un PIAO). */
    @Query("""
            SELECT s
            FROM StorageMinerva s
            WHERE s.identitafk = :identitafk
    """)
    List<StorageMinerva> findAllByIdentitafk(@Param("identitafk") Long identitafk);

    /** Tutti i record di un'identità per uno specifico codiceipa. */
    @Query("""
            SELECT s
            FROM StorageMinerva s
            WHERE s.identitafk = :identitafk
              AND s.codiceipa = :codiceipa
    """)
    List<StorageMinerva> findAllByIdentitafkAndCodiceipa(@Param("identitafk") Long identitafk,
                                                         @Param("codiceipa") String codiceipa);
}

