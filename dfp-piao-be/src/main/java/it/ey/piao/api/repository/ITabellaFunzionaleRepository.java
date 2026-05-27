package it.ey.piao.api.repository;

import it.ey.entity.TabellaFunzionale;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ITabellaFunzionaleRepository extends BaseRepository<TabellaFunzionale, Long> {

    /**
     * Trova tutte le TabellaFunzionale associate a una specifica entità e tipologia.
     */
    @Query("""
        SELECT DISTINCT t FROM TabellaFunzionale t
        LEFT JOIN FETCH t.ovp o
        LEFT JOIN FETCH t.stakeholder sh
        WHERE t.idEntitaFK = :idEntitaFK AND t.codTipologiaFK = :codTipologiaFK
        """)
    List<TabellaFunzionale> findByIdEntitaFKAndCodTipologiaFK(@Param("idEntitaFK") Long idEntitaFK,
                                                               @Param("codTipologiaFK") String codTipologiaFK);





    List<TabellaFunzionale> findByOvpId(Long idOvp);



    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione31 s31 ON s31.id = tf.idEntitaFK
    WHERE tf.ovp.id = :idOvp
    AND s31.idStato IN :statiId
    """)
    boolean existsOvpInSezione31StatoIn(Long idOvp, List<Long> statiId);

    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione331 s331 ON s331.id = tf.idEntitaFK
    WHERE tf.ovp.id = :idOvp
      AND s331.idStato IN :statiId
    """)
    boolean existsOvpInSezione331StatoIn(Long idOvp, List<Long> statiId);

    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione332 s332 ON s332.id = tf.idEntitaFK
    WHERE tf.ovp.id = :idOvp
      AND s332.idStato IN :statiId
    """)
    boolean existsOvpInSezione332StatoIn(Long idOvp, List<Long> statiId);



    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione32 s32 ON s32.id = tf.idEntitaFK
    WHERE tf.ovp.id = :idOvp
      AND s32.idStato IN :statiId
    """)
    boolean existsOvpInSezione32StatoIn(Long idOvp, List<Long> statiId);


    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione31 s31 ON s31.id = tf.idEntitaFK
    WHERE tf.stakeholder.id = :idStakeholder
      AND s31.idStato IN :statiId
    """)
    boolean existsStakeholderInSezione31StatoIn(Long idStakeholder, List<Long> statiId);


    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione32 s32 ON s32.id = tf.idEntitaFK
    WHERE tf.stakeholder.id = :idStakeholder
      AND s32.idStato IN :statiId
    """)
    boolean existsStakeholderInSezione32StatoIn(Long idStakeholder, List<Long> statiId);

    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione331 s331 ON s331.id = tf.idEntitaFK
    WHERE tf.stakeholder.id = :idStakeholder
      AND s331.idStato IN :statiId
    """)
    boolean existsStakeholderInSezione331StatoIn(Long idStakeholder, List<Long> statiId);


    @Query("""
    SELECT COUNT(tf) > 0
    FROM TabellaFunzionale tf
    JOIN Sezione332 s332 ON s332.id = tf.idEntitaFK
    WHERE tf.stakeholder.id = :idStakeholder
      AND s332.idStato IN :statiId
    """)
    boolean existsStakeholderInSezione332StatoIn(Long idStakeholder, List<Long> statiId);


    @Modifying
    @Query("""
    UPDATE TabellaFunzionale tf
    SET tf.ovp = null
    WHERE tf.ovp.id = :idOvp
    """)
    int setOvpToNullByOvpId(@Param("idOvp") Long idOvp);

    @Query("""
    SELECT DISTINCT tf.codTipologiaFK
    FROM TabellaFunzionale tf
    WHERE tf.ovp.id = :idOvp
    """)
    List<String> findSezioniByOvpId(@Param("idOvp") Long idOvp);


    @Query("""
    SELECT DISTINCT tf.codTipologiaFK
    FROM TabellaFunzionale tf
    WHERE tf.stakeholder.id = :stakeholderId
    """)
    List<String> findSezioniByStakeholderId(Long stakeholderId);


    @Modifying
    @Query("""
    UPDATE TabellaFunzionale tf
    SET tf.stakeholder = null
    WHERE tf.stakeholder.id = :stakeholderId
    """)
    int setStakeholderToNullByStakeholderId(Long stakeholderId);



    @Modifying
    @Query("""
    UPDATE TabellaFunzionale tabella
    SET tabella.active = false,
        tabella.deactivationTime = :deactivationTime
    WHERE tabella.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);



}
