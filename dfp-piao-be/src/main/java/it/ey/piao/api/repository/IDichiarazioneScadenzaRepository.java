package it.ey.piao.api.repository;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.entity.DichiarazioneScadenza;
import it.ey.piao.api.repository.projection.SollecitoDichiarazioneProjection;
import it.ey.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IDichiarazioneScadenzaRepository extends BaseRepository<DichiarazioneScadenza, Long>
{
    @Query(value = """
            SELECT ds.*
                FROM DichiarazioneScadenza ds
            WHERE ds.IdPiao = :idPiao
    """, nativeQuery = true)
    DichiarazioneScadenza findByPiao_Id(
       @Param("idPiao") Long idPiao
    );

    @Modifying
    @Query("UPDATE DichiarazioneScadenza d SET d.active = false, d.deactivationTime = :deactivationTime WHERE d.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

    @Query("""
            SELECT d
            FROM DichiarazioneScadenza d
            JOIN FETCH d.piao p
            ORDER BY d.createdTs DESC
    """)
    java.util.List<DichiarazioneScadenza> findAllStorico();

    @Modifying
    @Query("UPDATE DichiarazioneScadenza d SET d.stato = :stato WHERE d.id = :id")
    int updateStato(@Param("id") Long id, @Param("stato") Boolean stato);

    /**
     * Ricerca i PIAO (con stato != 8) per denominazione (LIKE) e filtri opzionali su:
     * <ul>
     *     <li>codPAFK della PA (filtro "Seleziona un'Amministrazione")</li>
     *     <li>tipologiaIstat dell'Anagrafica collegata alla Sezione1 del PIAO (filtro "Tipologia di Amministrazioni")</li>
     * </ul>
     * Ritorna la riga della DichiarazioneScadenza eventualmente collegata (può essere NULL),
     * cosicché il service possa applicare il filtro INVIATA / NON_INVIATA e restituire lo stato.
     * <p>
     * Risultato: Object[] con [Piao, DichiarazioneScadenza (può essere null), tipologiaIstat]
     */
    @Query("""
            SELECT p, ds, a.tipologiaIstat
            FROM Piao p
            LEFT JOIN Anagrafica a ON a.idPiao.id = p.id
            LEFT JOIN DichiarazioneScadenza ds ON ds.piao.id = p.id
            WHERE LOWER(p.denominazione) LIKE LOWER(CONCAT('%', :denominazione, '%'))
              AND p.idStato <> 8
              AND (:codPAFK IS NULL OR p.codPAFK = :codPAFK)
              AND (:tipologiaIstat IS NULL OR a.tipologiaIstat = :tipologiaIstat)
            ORDER BY p.createdTs DESC
    """)
    List<Object[]> searchDichiarazioniByPiao(@Param("denominazione") String denominazione,
                                             @Param("codPAFK") String codPAFK,
                                             @Param("tipologiaIstat") String tipologiaIstat);

    /**
     * Variante PAGINATA della searchDichiarazioniByPiao.
     * Usa proiezione interface-based + EXISTS + filtro stato in SQL per scalare su 8k+ record.
     * <p>
     * Parametri:
     * <ul>
     *     <li><b>denominazione</b>: LIKE %...% case-insensitive (obbligatorio dal service)</li>
     *     <li><b>codPAFK</b>: filtro opzionale (NULL = ignorato)</li>
     *     <li><b>tipologiaIstat</b>: filtro opzionale (NULL = ignorato)</li>
     *     <li><b>inviata</b>: NULL = nessun filtro, TRUE = solo INVIATA, FALSE = solo NON_INVIATA</li>
     *     <li><b>pageable</b>: page/size/sort. Default sort consigliato: createdTs DESC</li>
     * </ul>
     * NOTA: il sort dinamico via Pageable funziona sui campi proiettati (idPiao, codePA,
     * amministrazione). Per ordinare per createdTs lasciare il sort vuoto (la query ha già
     * ORDER BY p.createdTs DESC come default).
     * <p>
     * Indici DB consigliati per 8k+ record:
     * <pre>
     *   CREATE INDEX IF NOT EXISTS idx_piao_codpafk           ON piao(codPAFK);
     *   CREATE INDEX IF NOT EXISTS idx_piao_idstato           ON piao(idStato);
     *   CREATE INDEX IF NOT EXISTS idx_dichscad_idpiao        ON dichiarazione_scadenza(idPiao);
     *   CREATE INDEX IF NOT EXISTS idx_sezione1_idpiao        ON sezione1(idPiao);
     *   CREATE INDEX IF NOT EXISTS idx_anagrafica_idpiao      ON anagrafica(idPiao);
     * </pre>
     */
    @Query(value = """
            SELECT p.id              AS idPiao,
                   p.codPAFK         AS codePA,
                   p.denominazionePA AS amministrazione,
                   CASE WHEN EXISTS (SELECT 1 FROM DichiarazioneScadenza ds WHERE ds.piao.id = p.id)
                        THEN TRUE ELSE FALSE END AS inviata
            FROM Piao p
            WHERE LOWER(p.denominazione) LIKE LOWER(CONCAT('%', :denominazione, '%'))
              AND p.idStato <> 8
              AND (:codPAFK IS NULL OR p.codPAFK = :codPAFK)
              AND (:tipologiaIstat IS NULL OR EXISTS (
                    SELECT 1 FROM Anagrafica a
                    WHERE a.idPiao.id = p.id
                      AND a.tipologiaIstat = :tipologiaIstat))
              AND (:inviata IS NULL
                   OR (:inviata = TRUE  AND EXISTS (SELECT 1 FROM DichiarazioneScadenza ds2 WHERE ds2.piao.id = p.id))
                   OR (:inviata = FALSE AND NOT EXISTS (SELECT 1 FROM DichiarazioneScadenza ds3 WHERE ds3.piao.id = p.id)))
            """,
        countQuery = """
            SELECT COUNT(p)
            FROM Piao p
            WHERE LOWER(p.denominazione) LIKE LOWER(CONCAT('%', :denominazione, '%'))
              AND p.idStato <> 8
              AND (:codPAFK IS NULL OR p.codPAFK = :codPAFK)
              AND (:tipologiaIstat IS NULL OR EXISTS (
                    SELECT 1 FROM Anagrafica a
                    WHERE a.idPiao.id = p.id
                      AND a.tipologiaIstat = :tipologiaIstat))
              AND (:inviata IS NULL
                   OR (:inviata = TRUE  AND EXISTS (SELECT 1 FROM DichiarazioneScadenza ds2 WHERE ds2.piao.id = p.id))
                   OR (:inviata = FALSE AND NOT EXISTS (SELECT 1 FROM DichiarazioneScadenza ds3 WHERE ds3.piao.id = p.id)))
            """)
    Page<SollecitoDichiarazioneProjection> searchDichiarazioniByPiaoPaged(@Param("denominazione") String denominazione,
                                                                          @Param("codPAFK") String codPAFK,
                                                                          @Param("tipologiaIstat") String tipologiaIstat,
                                                                          @Param("inviata") Boolean inviata,
                                                                          Pageable pageable);
}
