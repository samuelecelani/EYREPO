package it.ey.piao.api.repository;

import it.ey.entity.EventoRischio;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MisuraPrevenzioneEventoRischioStakeholder;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMisuraPrevenzioneEventoRischioRepository extends BaseRepository<MisuraPrevenzioneEventoRischio,Long> {

   @Query("SELECT m FROM MisuraPrevenzioneEventoRischio m WHERE m.eventoRischio.id = :idEventoRischio")
   List<MisuraPrevenzioneEventoRischio> findByEventoRischioId(@Param("idEventoRischio") Long idEventoRischio);

    @Modifying
    @Query("UPDATE MisuraPrevenzioneEventoRischio m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.eventoRischio.id = :idEventoRischio")
    void deleteByEventoRischioId(@Param("idEventoRischio") Long idEventoRischio,
                                 @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("UPDATE MisuraPrevenzioneEventoRischio m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
   /**
    * Imposta a NULL il riferimento obiettivoPrevenzioneCorruzioneTrasparenza per tutte le MisuraPrevenzioneEventoRischio collegate.
    * Questo evita che le misure vengano cancellate quando si cancella un ObiettivoPrevenzioneCorruzioneTrasparenza.
    */
   @Modifying
   @Query("UPDATE MisuraPrevenzioneEventoRischio m SET m.obiettivoPrevenzioneCorruzioneTrasparenza = NULL WHERE m.obiettivoPrevenzioneCorruzioneTrasparenza.id = :idObiettivo")
   int setObiettivoPrevenzioneCorruzioneTrasparenzaToNullByObiettivoId(@Param("idObiettivo") Long idObiettivo);


    @Query("""
    SELECT mpStake
    FROM MisuraPrevenzioneEventoRischioStakeholder mpStake
    WHERE mpStake.stakeholder.id = :stakeholderId
    """)
    List<MisuraPrevenzioneEventoRischioStakeholder> findByStakeholderId(@Param("stakeholderId") Long stakeholderId);


    @Query("""
    SELECT DISTINCT s23.piao.id
    FROM MisuraPrevenzioneEventoRischioStakeholder ms
    JOIN ms.misuraPrevenzioneEventoRischio mper
    JOIN mper.eventoRischio er
    JOIN er.sezione23 s23
    WHERE ms.stakeholder.id = :idStakeholder
""")
    List<Long> findAllPiaoIdsByStakeholderInMisuraEventoRischio(@Param("idStakeholder") Long idStakeholder);



    // SOFT DELETE eliminazione relazione
    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneEventoRischio mis
        SET mis.active = false,
            mis.deactivationTime = :deactivationTime
        WHERE mis.obiettivoPrevenzioneCorruzioneTrasparenza.id = :idObiettivoPrevenzioneCorruzioneTrasparenza
          AND mis.active = true
    """)
    void softDeleteByObiettivoPrevenzioneCorruzioneTrasparenzaId(@Param("idObiettivoPrevenzioneCorruzioneTrasparenza") Long idObiettivoPrevenzioneCorruzioneTrasparenza,
                           @Param("deactivationTime") LocalDateTime deactivationTime);


    @Query("""
        SELECT m.id
        FROM MisuraPrevenzioneEventoRischio m
        WHERE m.eventoRischio.id = :idEventoRischio
          AND m.active = true
    """)
    List<Long> findActiveIdsByEventoRischioId(@Param("idEventoRischio") Long idEventoRischio);


    @Query("""
        SELECT m.id
        FROM MisuraPrevenzioneEventoRischio m
        WHERE m.obiettivoPrevenzioneCorruzioneTrasparenza.id = :idObiettivoPrevenzioneCorruzioneTrasparenza
          AND m.active = true
    """)
    List<Long> findActiveIdsByIdObiettivoPrevenzioneCorruzioneTrasparenza(@Param("idObiettivoPrevenzioneCorruzioneTrasparenza") Long idObiettivoPrevenzioneCorruzioneTrasparenza);



}
