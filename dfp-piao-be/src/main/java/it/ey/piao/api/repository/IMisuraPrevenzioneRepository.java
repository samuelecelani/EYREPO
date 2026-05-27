package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzione;
import it.ey.entity.ObiettivoPrevenzione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMisuraPrevenzioneRepository extends BaseRepository<MisuraPrevenzione,Long> {

    @Query("SELECT m FROM MisuraPrevenzione m WHERE m.obiettivoPrevenzione.id = :idObiettivoPrevenzione")
   List<MisuraPrevenzione> getMisuraPrevenzioneByObiettivoPrevenzione(@Param("idObiettivoPrevenzione") Long idObiettivoPrevenzione);

    @Query("SELECT o FROM MisuraPrevenzione o WHERE o.sezione23.id = :idSezione23")
    List<MisuraPrevenzione> getMisuraPrevenzioneByIdSezione23(@Param("idSezione23") Long idSezione23);

    @Modifying
    @Query("UPDATE MisuraPrevenzione m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("UPDATE MisuraPrevenzione m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.obiettivoPrevenzione.id = :idObiettivoPrevenzione")
    void softDeleteByObiettivoPrevenzioneId(@Param("idObiettivoPrevenzione") Long idObiettivoPrevenzione,
                        @Param("deactivationTime") LocalDateTime deactivationTime);










   /**
    * Imposta a NULL il riferimento obiettivoPrevenzione per tutte le MisuraPrevenzione collegate.
    * Evita che le misure vengano cancellate quando si elimina un ObiettivoPrevenzione.
    */
   @Modifying
   @Query("UPDATE MisuraPrevenzione m SET m.obiettivoPrevenzione = NULL WHERE m.obiettivoPrevenzione.id = :idObiettivo")
   int setObiettivoPrevenzioneToNullByObiettivoId(@Param("idObiettivo") Long idObiettivo);

    /**
     * Verifica se uno Stakeholder è utilizzato in una MisuraPrevenzione
     * collegata a Sezione23 con stato bloccante (VALIDATA o superiore)
     */
    @Query("""
     SELECT CASE WHEN COUNT(mpStake) > 0 THEN true ELSE false END
     FROM MisuraPrevenzioneStakeholder mpStake
     JOIN mpStake.misuraPrevenzione mp
     JOIN mp.sezione23 s23
     WHERE mpStake.stakeholder.id = :stakeholderId
     AND s23.idStato IN :statiIds
     """)
    boolean existsByStakeholderIdInMisura(@Param("stakeholderId") Long stakeholderId,@Param("statiIds") List<Long> statiIds);

    @Query("""
    SELECT DISTINCT s23.piao.id
    FROM MisuraPrevenzioneStakeholder mpStake
    JOIN mpStake.misuraPrevenzione mp
    JOIN mp.sezione23 s23
    WHERE mpStake.stakeholder.id = :idStakeholder
""")
    List<Long> findAllPiaoIdsByStakeholderInMisuraPrevenzione(@Param("idStakeholder") Long idStakeholder);

}


