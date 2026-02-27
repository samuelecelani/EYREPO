package it.ey.piao.api.repository;

import it.ey.entity.EventoRischio;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMisuraPrevenzioneEventoRischioRepository extends BaseRepository<MisuraPrevenzioneEventoRischio,Long> {

   List<MisuraPrevenzioneEventoRischio> getMisuraPrevenzioneByEventoRischio(EventoRischio eventoRischio);

   List<MisuraPrevenzioneEventoRischio> findByEventoRischioId(Long idEventoRischio);

   void deleteByEventoRischioId(Long idEventoRischio);

   /**
    * Imposta a NULL il riferimento obiettivoPrevenzioneCorruzioneTrasparenza per tutte le MisuraPrevenzioneEventoRischio collegate.
    * Questo evita che le misure vengano cancellate quando si cancella un ObiettivoPrevenzioneCorruzioneTrasparenza.
    */
   @Modifying
   @Query("UPDATE MisuraPrevenzioneEventoRischio m SET m.obiettivoPrevenzioneCorruzioneTrasparenza = NULL WHERE m.obiettivoPrevenzioneCorruzioneTrasparenza.id = :idObiettivo")
   int setObiettivoPrevenzioneCorruzioneTrasparenzaToNullByObiettivoId(@Param("idObiettivo") Long idObiettivo);
}
