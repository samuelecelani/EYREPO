package it.ey.piao.api.repository;

import it.ey.entity.ObbligoLegge;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IObbligoLeggeRepository extends BaseRepository<ObbligoLegge,Long> {

    @Query("SELECT o FROM ObbligoLegge o WHERE o.sezione23.id = :idSezione23")
    List<ObbligoLegge> getObiettivoPrevenzioneBySezione23(@Param("idSezione23") Long idSezione23);

    @Modifying
    @Query("UPDATE ObbligoLegge o SET o.active = false, o.deactivationTime = :deactivationTime WHERE o.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
