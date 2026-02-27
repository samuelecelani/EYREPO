package it.ey.piao.api.repository;

import it.ey.entity.OVP;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

}
