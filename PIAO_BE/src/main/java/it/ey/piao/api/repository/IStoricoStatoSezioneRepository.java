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
         * Usa IN con idEntitaFK per recuperare tutti gli stati in una query.
         */
        @Query("""
    SELECT s
    FROM StoricoStatoSezione s
    WHERE s.idEntitaFK IN :sezioniIds
      AND s.createdTs = (
          SELECT MAX(s2.createdTs)
          FROM StoricoStatoSezione s2
          WHERE s2.idEntitaFK = s.idEntitaFK
            AND s2.codTipologiaFK = s.codTipologiaFK
      )
""")
        List<StoricoStatoSezione> findLatestBySezioniIds(@Param("sezioniIds") List<Long> sezioniIds);


    }
