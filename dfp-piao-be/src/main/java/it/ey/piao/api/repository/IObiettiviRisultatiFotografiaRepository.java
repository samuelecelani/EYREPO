package it.ey.piao.api.repository;

import it.ey.entity.ObiettiviRisultatiFotografia;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

public interface IObiettiviRisultatiFotografiaRepository extends BaseRepository<ObiettiviRisultatiFotografia,Long> {

    @Query("""
           SELECT orf
           FROM ObiettiviRisultatiFotografia orf
           WHERE orf.sezione332.id = :idSezione332 AND orf.codTipologiaFK IN('OBIETTIVI_RISULTATI')
           """)
    List<ObiettiviRisultatiFotografia> getObiettiviRisultatiByIdSezione332(@Param("idSezione332") Long idSezione332);

    @Query("""
           SELECT orf
           FROM ObiettiviRisultatiFotografia orf
           WHERE orf.sezione332.id = :idSezione332 AND orf.codTipologiaFK IN('FOTOGRAFIA_FORMAZIONE')
           """)
    List<ObiettiviRisultatiFotografia> getFotografiaFormazioneByIdSezione332(@Param("idSezione332") Long idSezione332);

    @Modifying
    @Query("UPDATE ObiettiviRisultatiFotografia orf SET orf.active = false, orf.deactivationTime = :deactivationTime WHERE orf.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}



