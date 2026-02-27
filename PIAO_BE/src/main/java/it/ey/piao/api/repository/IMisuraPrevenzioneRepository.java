package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzione;
import it.ey.entity.ObiettivoPrevenzione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMisuraPrevenzioneRepository extends BaseRepository<MisuraPrevenzione,Long> {

   List<MisuraPrevenzione> getMisuraPrevenzioneByObiettivoPrevenzione(ObiettivoPrevenzione obiettivoPrevenzione);

   /**
    * Imposta a NULL il riferimento obiettivoPrevenzione per tutte le MisuraPrevenzione collegate.
    * Evita che le misure vengano cancellate quando si elimina un ObiettivoPrevenzione.
    */
   @Modifying
   @Query("UPDATE MisuraPrevenzione m SET m.obiettivoPrevenzione = NULL WHERE m.obiettivoPrevenzione.id = :idObiettivo")
   int setObiettivoPrevenzioneToNullByObiettivoId(@Param("idObiettivo") Long idObiettivo);
}
