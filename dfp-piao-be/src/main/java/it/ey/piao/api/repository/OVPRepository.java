package it.ey.piao.api.repository;

import it.ey.entity.OVP;
import it.ey.entity.Sezione21;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OVPRepository extends BaseRepository<OVP, Long> {

    /**
     * Trova tutti gli OVP associati a una specifica Sezione21.
     * Query semplice senza JOIN FETCH per performance ottimali.
     */
    @Query("""
        SELECT DISTINCT o FROM OVP o
        LEFT JOIN o.sezione21 s21
        WHERE o.sezione21.id = :idSezione21
        """)
    List<OVP> findBySezione21Id(@Param("idSezione21") Long idSezione21);

    /**
     * Trova tutti gli OVP associati a un PIAO attraverso Sezione21.
     * Query semplice senza JOIN FETCH per performance ottimali.
     */
    @Query("""
        SELECT DISTINCT o FROM OVP o
        LEFT JOIN o.sezione21 s21
        LEFT JOIN s21.piao p
        WHERE s21.piao.id = :piaoId
        """)
    List<OVP> findByPiaoId(@Param("piaoId") Long piaoId);

    /**
     * Aggiorna solo valoreIndice e descrizioneIndice per un OVP specifico.
     * Query ottimizzata che modifica solo i campi necessari senza caricare l'intera entità.
     *
     * @param idOvp ID dell'OVP da aggiornare
     * @param valoreIndice nuovo valore per valoreIndice
     * @param descrizioneIndice nuova descrizione per descrizioneIndice
     * @return numero di righe modificate (1 se successo, 0 se OVP non trovato)
     */
    @Modifying
    @Query("""
        UPDATE OVP o
        SET o.valoreIndice = :valoreIndice,
            o.descrizioneIndice = :descrizioneIndice
        WHERE o.id = :idOvp
        """)
    int updateValoreIndiceAndDescrizione(@Param("idOvp") Long idOvp,
                                         @Param("valoreIndice") Long valoreIndice,
                                         @Param("descrizioneIndice") String descrizioneIndice);


    @Query("""
    SELECT o.sezione21.piao.id
    FROM OVPStakeHolder sh
    JOIN sh.ovp o
    WHERE sh.stakeholder.id = :idStakeholder
""")
    Optional<Long> findIdPiao21ByStakeholderId(@Param("idStakeholder") Long idStakeholder);



    @Query("""
    SELECT CASE WHEN COUNT(sh) > 0 THEN true ELSE false END
    FROM OVPStakeHolder sh
    JOIN sh.ovp o
    WHERE sh.stakeholder.id = :stakeholderId
    AND o.sezione21.idStato IN :statiIds
    """)
    boolean existsByStakeholderIdInOVP(@Param("stakeholderId") Long stakeholderId, @Param("statiIds") List<Long> statiIds);

    @Query("""
    SELECT CASE WHEN COUNT(ovp) > 0 THEN true ELSE false END
    FROM OVP ovp
    JOIN ovp.prioritaPolitiche opp
    JOIN ovp.sezione21 s21
    WHERE opp.prioritaPolitica.id = :idPriorita
    AND s21.idStato IN :statiId
    """)
    boolean existsByPrioritaPoliticaIdAndSezione21StatoIn(@Param("idPriorita") Long idPriorita, @Param("statiId") List<Long> statiId);



    @Query("""
    SELECT CASE WHEN COUNT(ovp) > 0 THEN true ELSE false END
    FROM OVP ovp
    JOIN ovp.areeOrganizzative ao
    JOIN ovp.sezione21 s21
    WHERE ao.areaOrganizzativa.id = :idArea
    AND s21.idStato IN :statiId
    """)
    boolean existsByAreaOrganizzativaIdAndSezione21StatoIn(@Param("idArea") Long idArea, @Param("statiId") List<Long> statiId);


    @Modifying
    @Query("""
    UPDATE OVP ovp
    SET ovp.active = false,
        ovp.deactivationTime = :deactivationTime
    WHERE ovp.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
