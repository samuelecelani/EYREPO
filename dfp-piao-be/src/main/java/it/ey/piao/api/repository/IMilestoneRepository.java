package it.ey.piao.api.repository;

import it.ey.entity.Milestone;
import it.ey.entity.Promemoria;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMilestoneRepository extends BaseRepository<Milestone, Long>
{
    @Query("SELECT m FROM Milestone m WHERE m.sottofaseMonitoraggio.id = :idSottofaseMonitoraggio")
    List<Milestone> getMilestoneByIdSottofaseMonitoraggio(@Param("idSottofaseMonitoraggio") Long idSottofaseMonitoraggio);

    @Modifying
    @Query("UPDATE Milestone m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("UPDATE Milestone m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.sottofaseMonitoraggio.id = :idSottofaseMonitoraggio")
    void softDeleteBySottofaseMonitoraggioId(@Param("idSottofaseMonitoraggio") Long idSottofaseMonitoraggio,
                        @Param("deactivationTime") LocalDateTime deactivationTime);



}
