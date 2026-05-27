package it.ey.piao.api.repository;

import it.ey.entity.StoricoStatoSezione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


    public interface IStoricoStatoSezioneRepository extends BaseRepository<StoricoStatoSezione, Long> {

        @Query("""
    select s
    from StoricoStatoSezione s
    where s.idEntitaFK = :idEntitaFK
      and s.codTipologiaFK = :sezione
""")
        List<StoricoStatoSezione> findByIdEntitaAndCodTipologia(@Param("idEntitaFK") Long idEntitaFK,
                                                                @Param("sezione") String sezione);

        /**
         * Query batch ottimizzata che recupera gli ultimi stati per una lista di sezioni.
         * Per ogni coppia (idEntitaFK, codTipologiaFK) restituisce il record con
         * la createdTs più recente e, a parità, l'id più alto.
         */
        @Query("""
    SELECT s
    FROM StoricoStatoSezione s
    WHERE s.idEntitaFK IN :sezioniIds
      AND s.id = (
          SELECT MAX(s2.id)
          FROM StoricoStatoSezione s2
          WHERE s2.idEntitaFK = s.idEntitaFK
            AND s2.codTipologiaFK = s.codTipologiaFK
            AND s2.createdTs = (
                SELECT MAX(s3.createdTs)
                FROM StoricoStatoSezione s3
                WHERE s3.idEntitaFK = s.idEntitaFK
                  AND s3.codTipologiaFK = s.codTipologiaFK
            )
      )
""")
       List<StoricoStatoSezione> findLatestBySezioniIds(@Param("sezioniIds") List<Long> sezioniIds);

        /**
         * Recupera TUTTO lo storico per una lista di sezioni, ordinato per idEntitaFK, codTipologiaFK, createdTs.
         * Serve per determinare penultimo/ultimo stato nella vista di validazione.
         */
        @Query("""
    SELECT s
    FROM StoricoStatoSezione s
    WHERE s.idEntitaFK IN :sezioniIds
    ORDER BY s.idEntitaFK, s.codTipologiaFK, s.createdTs ASC, s.id ASC
""")
        List<StoricoStatoSezione> findAllBySezioniIdsOrdered(@Param("sezioniIds") List<Long> sezioniIds);

        @Query("""

   SELECT s
   FROM StoricoStatoSezione s
   WHERE s.idEntitaFK IN :sezioniIds
     AND s.codTipologiaFK IN :tipologie
     AND s.id = (
         SELECT MAX(s2.id)
         FROM StoricoStatoSezione s2
         WHERE s2.idEntitaFK = s.idEntitaFK
           AND s2.codTipologiaFK = s.codTipologiaFK
           AND s2.codTipologiaFK IN :tipologie
           AND s2.createdTs = (
               SELECT MAX(s3.createdTs)
               FROM StoricoStatoSezione s3
               WHERE s3.idEntitaFK = s.idEntitaFK
                 AND s3.codTipologiaFK = s.codTipologiaFK
                 AND s3.codTipologiaFK IN :tipologie
           )
     )

""")
        List <StoricoStatoSezione> findLastVersion(@Param("sezioniIds") List<Long> sezioniIds,
                                                   @Param("tipologie") List<String> tipologie
        );


    }
