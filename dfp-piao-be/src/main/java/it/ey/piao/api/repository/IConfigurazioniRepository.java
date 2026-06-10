package it.ey.piao.api.repository;

import it.ey.entity.Configurazioni;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IConfigurazioniRepository extends BaseRepository<Configurazioni, Long>
{
    @Query("SELECT c FROM Configurazioni c WHERE c.codice = :codice")
    Optional<Configurazioni> findByCodice(@Param("codice") String codice);


    @Query("""
            SELECT c.valore
                FROM Configurazioni c
                    WHERE c.codice = :codice
    """)
    String getValoreFromCodice(@Param("codice") String codice);

    @Modifying
    @Query("""
            UPDATE Configurazioni c
                SET c.valore = :valore
                    WHERE c.codice = :codice
    """)
    void setValoreFromCodice(@Param("codice") String codice,
                               @Param("valore") String valore);
}
