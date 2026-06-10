package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.enums.Tipologia;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PiaoRepository extends BaseRepository<Piao, Long> {

    @Query(value = """
            SELECT p.* FROM piao p
            WHERE p.codpafk = :codPAFK
              AND p.x_created_ts BETWEEN :startDate AND :endDate
              AND p.idstato <> :idStato
              AND p.x_active = true
            ORDER BY CAST(p.versione AS double precision) DESC
            LIMIT 1
    """, nativeQuery = true)
    Piao findPiaoByMancataDichiarazione(
        @Param("codPAFK")String codPAFK,
        @Param("startDate")LocalDate startDate,
        @Param("endDate")LocalDate endDate,
        @Param("idStato")Long idStato
    );

    // Recupera l'ultimo PIAO per PA e anno corrente
   public Piao findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
        String codPAFK,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Recupera tutti i PIAO per PA nel range di date ordinati per versione numerica DESC.
     * Usare il primo elemento della lista per ottenere il PIAO con la versione MAX.
     * NB: l'ordinamento è fatto come numero (CAST a double) per evitare l'ordinamento
     * lessicografico che farebbe risultare "10.0" < "2.0".
     */
    @Query("""
        SELECT p FROM Piao p
        WHERE p.codPAFK = :codPAFK
          AND p.createdTs BETWEEN :startDate AND :endDate
        ORDER BY CAST(p.versione AS double) DESC
    """)
    List<Piao> findMaxVersionInRange(
        @Param("codPAFK") String codPAFK,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

   public List<Piao> findByCodPAFK(String codPAF);



     // Una denominazione è considerata "corrente" se l'anno odierno compare come ANNO DI INIZIO del triennio, cioè a SINISTRA del trattino
     //
    @Query("""
        SELECT p FROM Piao p
        WHERE p.codPAFK = :codPAFK
          AND (
            :isCurrent = false
            OR (
              p.idStato <> 8
              AND (
                p.denominazione LIKE CONCAT('%/', :currentYear, '-%')
                OR p.denominazione LIKE CONCAT('% ', :currentYearShort, '-%')
              )
            )
          )
    """)
    List<Piao> findByCodPaFkAndIsCurrent(@Param("codPAFK") String codPAFK,
                                         @Param("isCurrent") boolean isCurrent,
                                         @Param("currentYear") String currentYear,
                                         @Param("currentYearShort") String currentYearShort);

    /**
     * Recupera il PIAO con la versione massima nel range di date indicato,
     * con versione strettamente minore di quella passata.
     * Utile per trovare la versione precedente di un PIAO nello stesso anno.
     */
    @Query("""
        SELECT p FROM Piao p
        WHERE p.codPAFK = :codPAFK
          AND p.createdTs BETWEEN :startDate AND :endDate
          AND CAST(p.versione AS double) < CAST(:versione AS double)
        ORDER BY CAST(p.versione AS double) DESC
    """)
    List<Piao> findPrevVersionInRange(
        @Param("codPAFK") String codPAFK,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("versione") String versione
    );


    /**
     * Query nativa ottimizzata che recupera tutte le informazioni delle sezioni in una sola query.
     * Ritorna una lista di array Object[] con: [numeroSezione, sezioneId, updatedTs]
     */
    @Query(value = """
        SELECT '1' as numeroSezione, s1.id as sezioneId, s1.x_updated_ts as updatedTs
        FROM sezione1 s1
        WHERE s1.idPiao = :piaoId
        UNION ALL
        SELECT '21', s21.id, s21.x_updated_ts
        FROM sezione21 s21
        WHERE s21.idPiao = :piaoId
        UNION ALL
        SELECT '22', s22.id, s22.x_updated_ts
        FROM sezione22 s22
        WHERE s22.idPiao = :piaoId
        UNION ALL
        SELECT '23', s23.id, s23.x_updated_ts
        FROM sezione23 s23
        WHERE s23.idPiao = :piaoId
        UNION ALL
        SELECT '4', s4.id, s4.x_updated_ts
        FROM sezione4 s4
        WHERE s4.idPiao = :piaoId
        UNION ALL
        SELECT '31', s31.id, s31.x_updated_ts
        FROM sezione31 s31
        WHERE s31.idPiao = :piaoId
        UNION ALL
        SELECT '32', s32.id, s32.x_updated_ts
        FROM sezione32 s32
        WHERE s32.idPiao = :piaoId
        UNION ALL
        SELECT '331', s331.id, s331.x_updated_ts
        FROM sezione331 s331
        WHERE s331.idPiao = :piaoId
        UNION ALL
        SELECT '332', s332.id, s332.x_updated_ts
        FROM sezione332 s332
        WHERE s332.idPiao = :piaoId
    """, nativeQuery = true)
    List<Object[]> findAllSezioniInfoByPiaoId(@Param("piaoId") Long piaoId);


    @Query("""
    SELECT p
    FROM Piao p
    WHERE LOWER(p.codPAFK) LIKE LOWER(CONCAT('%', :codPAFK, '%'))
      AND LOWER(p.denominazione) LIKE LOWER(CONCAT('%', :denominazione, '%'))
      AND (:versione IS NULL OR p.versione = :versione)
      AND (:tipologia IS NULL OR p.tipologia = :tipologia)
      AND p.idStato = 8
      """)
    List<Piao> findByCodPafkAndDenominazioneLikeAndOptionalVersione(  @Param("codPAFK") String codPAFK,
                                                                @Param("denominazione") String denominazione,
                                                                @Param("versione") String versione,
                                                                @Param("tipologia") Tipologia tipologia
                                                              );

    /**
     * Recupera una lista di PIAO per codPAFK e denominazione (LIKE).
     * Usato per l'esposizione esterna dei dati PIAO.
     */
    @Query("""
    SELECT p
    FROM Piao p
    WHERE p.codPAFK = :codPAFK
      AND LOWER(p.denominazione) LIKE LOWER(CONCAT('%', :denominazione, '%'))
    ORDER BY p.versione DESC
    """)
    List<Piao> findByCodPafkAndDenominazioneLike(@Param("codPAFK") String codPAFK,
                                                  @Param("denominazione") String denominazione);

    /**
     * Query nativa che recupera tutti i dati necessari per PiaoExternalDTO in una singola query.
     * Fa JOIN su: Piao -> Sezione1 -> Anagrafica, Sezione21 -> OVP -> OVPStrategia -> Indicatori.
     * Usa UNION ALL per includere anche gli obiettivi PERFORMANCE_INDIVIDUALE collegati tramite idObiettivoPeformance.
     *
     * Ritorna Object[] con tutti i campi necessari per costruire PiaoExternalDTO.
     */
    @Query(value = """
        SELECT * FROM (
                        SELECT
                            a.denominazioneente,
                            a.acronimopa,
                            a.codicefiscale,
                            a.codiceipa,
                            a.tipologiapa,
                            a.piva,
                            a.indirizzosedelegale,
                            a.indirizzourp,
                            a.www,
                            a.mail,
                            a.telefono,
                            a.pec,
                            a.nomerpct,
                            a.cognomerpct,
                            a.ruolorpct,
                            a.nomertd,
                            a.strutturarifrtd,
                            ovp.id as ovp_id,
                            ovp.codice as ovp_codice,
                            ovp.denominazione as ovp_denominazione,
                            strat.id as strategia_id,
                            strat.codstrategia,
                            strat.denominazionestrategia,
                            ind.id as indicatore_id,
                            ind.codtipologiafk as indicatore_codtipologia,
                            ind.denominazione as indicatore_denominazione,
                            ind.unitamisura as indicatore_unitamisura,
                            ind.peso as indicatore_peso,
                            ind.polarita as indicatore_polarita,
                            ind.baseline as indicatore_baseline,
                            ind.fontedati as indicatore_fontedati,
                            op.id as obiettivo_id,
                            op.codice as obiettivo_codice,
                            op.codtipologiafk as obiettivo_tipologia,
                            op.denominazione as obiettivo_denominazione,
                            op.idobiettivopeformance as obiettivo_id_perf,
                            op.risorseeconomicafinanziaria as obiettivo_risorse_eco,
                            op.risorsestrumentali as obiettivo_risorse_strum,
                            op.tipologiarisorsa as obiettivo_tipologia_risorsa,
                            p.versione
                        FROM piao p
                        LEFT JOIN sezione1 s1 ON s1.idpiao = p.id
                        LEFT JOIN anagrafica a ON a.idpiao = p.id
                        LEFT JOIN sezione21 s21 ON s21.idpiao = p.id
                        LEFT JOIN ovp ON ovp.idsezione21 = s21.id
                        LEFT JOIN ovpstrategia strat ON strat.idovp = ovp.id
                        LEFT JOIN ovpstrategiaindicatore osi ON osi.idovpstrategia = strat.id
                        LEFT JOIN indicatore ind ON ind.id = osi.idindicatore
                        LEFT JOIN obiettivoperformance op ON op.idstrategiaovp = strat.id
                        WHERE p.codpafk = :codPAFK

                        UNION ALL

                        SELECT
                            a.denominazioneente,
                            a.acronimopa,
                            a.codicefiscale,
                            a.codiceipa,
                            a.tipologiapa,
                            a.piva,
                            a.indirizzosedelegale,
                            a.indirizzourp,
                            a.www,
                            a.mail,
                            a.telefono,
                            a.pec,
                            a.nomerpct,
                            a.cognomerpct,
                            a.ruolorpct,
                            a.nomertd,
                            a.strutturarifrtd,
                            ovp.id as ovp_id,
                            ovp.codice as ovp_codice,
                            ovp.denominazione as ovp_denominazione,
                            strat.id as strategia_id,
                            strat.codstrategia,
                            strat.denominazionestrategia,
                            NULL as indicatore_id,
                            NULL as indicatore_codtipologia,
                            NULL as indicatore_denominazione,
                            NULL as indicatore_unitamisura,
                            NULL as indicatore_peso,
                            NULL as indicatore_polarita,
                            NULL as indicatore_baseline,
                            NULL as indicatore_fontedati,
                            op_child.id as obiettivo_id,
                            op_child.codice as obiettivo_codice,
                            op_child.codtipologiafk as obiettivo_tipologia,
                            op_child.denominazione as obiettivo_denominazione,
                            op_child.idobiettivopeformance as obiettivo_id_perf,
                            op_child.risorseeconomicafinanziaria as obiettivo_risorse_eco,
                            op_child.risorsestrumentali as obiettivo_risorse_strum,
                            op_child.tipologiarisorsa as obiettivo_tipologia_risorsa,
                            p.versione
                        FROM piao p
                        LEFT JOIN sezione1 s1 ON s1.idpiao = p.id
                        LEFT JOIN anagrafica a ON a.idpiao = p.id
                        LEFT JOIN sezione21 s21 ON s21.idpiao = p.id
                        LEFT JOIN ovp ON ovp.idsezione21 = s21.id
                        LEFT JOIN ovpstrategia strat ON strat.idovp = ovp.id
                        LEFT JOIN obiettivoperformance op_parent ON op_parent.idstrategiaovp = strat.id
                        LEFT JOIN obiettivoperformance op_child ON op_child.idobiettivopeformance = op_parent.id
                        WHERE p.codpafk = :codPAFK
                          AND op_child.id IS NOT NULL
                    ) AS combined
                    ORDER BY versione DESC, ovp_id, strategia_id, indicatore_id, obiettivo_id
    """, nativeQuery = true)
    List<Object[]> findPiaoExternalData(@Param("codPAFK") String codPAFK);

    @Query("""
    SELECT p
    FROM Piao p
    WHERE p.codPAFK = :codPAFK
    """)
    List<Piao> findPiaoLastVersion(@Param("codPAFK") String codPAFK, @Param("denominazione") String denominazione);

    @Query("""
    SELECT p
    FROM Piao p
    WHERE p.idStato = 8
      AND (:codPAFK IS NULL OR p.codPAFK = :codPAFK)
    ORDER BY p.createdTs DESC
    """)
    List<Piao> findAllPubblicati(@Param("codPAFK") String codPAFK);

    @Query("""
    SELECT p
    FROM Piao p
    WHERE p.idStato = 8
      AND (:idPiao IS NULL OR p.id = :idPiao)
      AND LOWER(p.denominazione) LIKE LOWER(CONCAT('%', :denominazione, '%'))
      AND LOWER(p.codPAFK) LIKE LOWER(CONCAT('%', :codePa, '%'))
    ORDER BY p.id DESC
    """)
    List<Piao> findAllPiaoPubblicati(@Param("idPiao") Long idPiao,
                                     @Param("denominazione") String denominazione,
                                     @Param("codePa") String codePa);

    /**
     * Ricerca PIAO pubblicati (idStato=8) con filtri opzionali su codPAFK e tipologiaIstat (da Anagrafica via Piao).
     */
    @Query("""
    SELECT DISTINCT p
    FROM Piao p
    LEFT JOIN Anagrafica a ON a.idPiao.id = p.id
    WHERE p.idStato = 8
      AND (:codiceIpa IS NULL OR p.codPAFK = :codiceIpa)
      AND (:tipologia IS NULL OR a.tipologiaIstat = :tipologia)
    ORDER BY p.createdTs DESC
    """)
    List<Piao> searchPubblicati(
            @Param("codiceIpa") String codiceIpa,
            @Param("tipologia") String tipologia
    );

    /**
     * Ricerca PIAO pubblicati (idStato=8) con denominazione obbligatoria e tipologia facoltativa.
     * Ritorna sempre l'informazione della tipologiaIstat dalla tabella Anagrafica.
     * Risultato: Object[] con [Piao, tipologiaIstat]
     */
    @Query("""
    SELECT p, a.tipologiaIstat
    FROM Piao p
    LEFT JOIN Anagrafica a ON a.idPiao.id = p.id
    WHERE p.idStato = 8
      AND LOWER(p.denominazione) LIKE LOWER(CONCAT('%', :denominazione, '%'))
      AND (:tipologia IS NULL OR a.tipologiaIstat = :tipologia)
    ORDER BY p.createdTs DESC
    """)
    List<Object[]> searchPubblicatiByDenominazione(
            @Param("denominazione") String denominazione,
            @Param("tipologia") String tipologia
    );

    /**
     * Query nativa che recupera i dati per PiaoExternalDTO filtrando per id PIAO.
     * Stessa struttura di findPiaoExternalData ma filtra per p.id = :idPiao.
     */
    @Query(value = """
        SELECT * FROM (
                        SELECT
                            a.denominazioneente,
                            a.acronimopa,
                            a.codicefiscale,
                            a.codiceipa,
                            a.tipologiapa,
                            a.piva,
                            a.indirizzosedelegale,
                            a.indirizzourp,
                            a.www,
                            a.mail,
                            a.telefono,
                            a.pec,
                            a.nomerpct,
                            a.cognomerpct,
                            a.ruolorpct,
                            a.nomertd,
                            a.strutturarifrtd,
                            ovp.id as ovp_id,
                            ovp.codice as ovp_codice,
                            ovp.denominazione as ovp_denominazione,
                            strat.id as strategia_id,
                            strat.codstrategia,
                            strat.denominazionestrategia,
                            ind.id as indicatore_id,
                            ind.codtipologiafk as indicatore_codtipologia,
                            ind.denominazione as indicatore_denominazione,
                            ind.unitamisura as indicatore_unitamisura,
                            ind.peso as indicatore_peso,
                            ind.polarita as indicatore_polarita,
                            ind.baseline as indicatore_baseline,
                            ind.fontedati as indicatore_fontedati,
                            op.id as obiettivo_id,
                            op.codice as obiettivo_codice,
                            op.codtipologiafk as obiettivo_tipologia,
                            op.denominazione as obiettivo_denominazione,
                            op.idobiettivopeformance as obiettivo_id_perf,
                            op.risorseeconomicafinanziaria as obiettivo_risorse_eco,
                            op.risorsestrumentali as obiettivo_risorse_strum,
                            op.tipologiarisorsa as obiettivo_tipologia_risorsa,
                            p.versione
                        FROM piao p
                        LEFT JOIN sezione1 s1 ON s1.idpiao = p.id
                        LEFT JOIN anagrafica a ON a.idpiao = p.id
                        LEFT JOIN sezione21 s21 ON s21.idpiao = p.id
                        LEFT JOIN ovp ON ovp.idsezione21 = s21.id
                        LEFT JOIN ovpstrategia strat ON strat.idovp = ovp.id
                        LEFT JOIN ovpstrategiaindicatore osi ON osi.idovpstrategia = strat.id
                        LEFT JOIN indicatore ind ON ind.id = osi.idindicatore
                        LEFT JOIN obiettivoperformance op ON op.idstrategiaovp = strat.id
                        WHERE p.id = :idPiao

                        UNION ALL

                        SELECT
                            a.denominazioneente,
                            a.acronimopa,
                            a.codicefiscale,
                            a.codiceipa,
                            a.tipologiapa,
                            a.piva,
                            a.indirizzosedelegale,
                            a.indirizzourp,
                            a.www,
                            a.mail,
                            a.telefono,
                            a.pec,
                            a.nomerpct,
                            a.cognomerpct,
                            a.ruolorpct,
                            a.nomertd,
                            a.strutturarifrtd,
                            ovp.id as ovp_id,
                            ovp.codice as ovp_codice,
                            ovp.denominazione as ovp_denominazione,
                            strat.id as strategia_id,
                            strat.codstrategia,
                            strat.denominazionestrategia,
                            NULL as indicatore_id,
                            NULL as indicatore_codtipologia,
                            NULL as indicatore_denominazione,
                            NULL as indicatore_unitamisura,
                            NULL as indicatore_peso,
                            NULL as indicatore_polarita,
                            NULL as indicatore_baseline,
                            NULL as indicatore_fontedati,
                            op_child.id as obiettivo_id,
                            op_child.codice as obiettivo_codice,
                            op_child.codtipologiafk as obiettivo_tipologia,
                            op_child.denominazione as obiettivo_denominazione,
                            op_child.idobiettivopeformance as obiettivo_id_perf,
                            op_child.risorseeconomicafinanziaria as obiettivo_risorse_eco,
                            op_child.risorsestrumentali as obiettivo_risorse_strum,
                            op_child.tipologiarisorsa as obiettivo_tipologia_risorsa,
                            p.versione
                        FROM piao p
                        LEFT JOIN sezione1 s1 ON s1.idpiao = p.id
                        LEFT JOIN anagrafica a ON a.idpiao = p.id
                        LEFT JOIN sezione21 s21 ON s21.idpiao = p.id
                        LEFT JOIN ovp ON ovp.idsezione21 = s21.id
                        LEFT JOIN ovpstrategia strat ON strat.idovp = ovp.id
                        LEFT JOIN obiettivoperformance op_parent ON op_parent.idstrategiaovp = strat.id
                        LEFT JOIN obiettivoperformance op_child ON op_child.idobiettivopeformance = op_parent.id
                        WHERE p.id = :idPiao
                          AND op_child.id IS NOT NULL
                    ) AS combined
                    ORDER BY versione DESC, ovp_id, strategia_id, indicatore_id, obiettivo_id
    """, nativeQuery = true)
    List<Object[]> findPiaoExternalDataById(@Param("idPiao") Long idPiao);

    @Modifying
    @Query("""
    UPDATE Piao p SET p.idStato = :idStato,
                          p.updatedByNameSurname = :userNameSurname,
                          p.updatedByRole = :userRole
    WHERE p.id = :idPiao
""")
    void updateStatoPiao(@Param("idPiao") Long idPiao,
                            @Param("idStato") Long idStato,
                            @Param("userNameSurname") String userNameSurname,
                            @Param("userRole") String userRole);

    @Modifying
    @Query("""
    UPDATE Piao p SET p.idStato = :idStato,
                          p.url = :url,
                          p.dataApprovazione = :dataApprovazione,
                          p.isCompilatoNormativa = :isCompilatoNormativa
    WHERE p.id = :idPiao
""")
    void updatePubblicazionePiao(@Param("idPiao") Long idPiao,
                                 @Param("idStato") Long idStato,
                                 @Param("url") String url,
                                 @Param("dataApprovazione") LocalDate dataApprovazione,
                                 @Param("isCompilatoNormativa") Boolean isCompilatoNormativa);
}
