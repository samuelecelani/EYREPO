package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MonitoraggioPrevenzione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMonitoraggioPrevenzioneRepository extends BaseRepository<MonitoraggioPrevenzione, Long>
{

    @Query("SELECT m FROM MonitoraggioPrevenzione m WHERE m.misuraPrevenzioneEventoRischio.id = :idMisuraPrevenzioneEventoRischio")
    List<MonitoraggioPrevenzione> getMonitoraggioByMisuraPrevenzioneEventoRischio(@Param("idMisuraPrevenzioneEventoRischio") Long idMisuraPrevenzioneEventoRischio);

    @Modifying
    @Query("UPDATE MonitoraggioPrevenzione m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("UPDATE MonitoraggioPrevenzione m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.misuraPrevenzioneEventoRischio.id = :idMisuraPrevenzione")
    void softDeleteByMisuraPrevenzioneId(@Param("idMisuraPrevenzione") Long idMisuraPrevenzione,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
