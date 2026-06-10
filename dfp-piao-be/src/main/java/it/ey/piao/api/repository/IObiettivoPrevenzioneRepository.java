package it.ey.piao.api.repository;

import it.ey.entity.ObiettivoPrevenzione;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IObiettivoPrevenzioneRepository extends BaseRepository<ObiettivoPrevenzione,Long> {

    @Query("SELECT o FROM ObiettivoPrevenzione o WHERE o.sezione23.id = :idSezione23")
    List<ObiettivoPrevenzione> getObiettivoPrevenzioneByIdSezione23(@Param("idSezione23") Long idSezione23);

    @Modifying
    @Query("UPDATE ObiettivoPrevenzione o SET o.active = false, o.deactivationTime = :deactivationTime WHERE o.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
