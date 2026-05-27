package it.ey.piao.api.repository;

import it.ey.entity.AttivitaSensibile;
import it.ey.entity.EventoRischio;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IEventoRischioRepository extends BaseRepository<EventoRischio, Long> {

    @Query("SELECT e FROM EventoRischio e WHERE e.attivitaSensibile.id = :idAttivitaSensibile ")
    List<EventoRischio> findByAttivitaSensibileId(Long idAttivitaSensibile);

    @Modifying
    @Query("UPDATE EventoRischio e SET e.active = false, e.deactivationTime = :deactivationTime WHERE e.attivitaSensibile.id = :idAttivitaSensibile ")
    void deleteByAttivitaSensibileId(@Param("idAttivitaSensibile") Long idAttivitaSensibile,
                                     @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("UPDATE EventoRischio e SET e.active = false, e.deactivationTime = :deactivationTime WHERE e.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("UPDATE EventoRischio e" +
        " SET e.active = false," +
        " e.deactivationTime = :deactivationTime " +
        "WHERE e.attivitaSensibile.id = :idAttivitaSensibile")
    void softDeleteByAttivitaSensibileId(@Param("idAttivitaSensibile") Long idAttivitaSensibile,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
