package it.ey.piao.api.repository;

import it.ey.entity.Sezione4;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ISottofaseMonitoraggioRepository extends BaseRepository<SottofaseMonitoraggio, Long> {


    @Query("""
           SELECT s
           FROM SottofaseMonitoraggio s
           WHERE s.sezione4 = :sezione4
           """)

    List<SottofaseMonitoraggio> getSottofaseMonitoraggioBySezione4(Sezione4 sezione4);


    @Modifying
    @Query("""
    UPDATE SottofaseMonitoraggio sottofase
    SET sottofase.active = false,
        sottofase.deactivationTime = :deactivationTime
    WHERE sottofase.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
