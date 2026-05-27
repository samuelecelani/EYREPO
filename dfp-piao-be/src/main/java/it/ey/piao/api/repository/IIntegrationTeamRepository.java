package it.ey.piao.api.repository;

import it.ey.entity.IntegrationTeam;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IIntegrationTeamRepository extends BaseRepository<IntegrationTeam, Long> {

    @Modifying
    @Query("""
    UPDATE IntegrationTeam it
    SET it.active = false,
        it.deactivationTime = :deactivationTime
    WHERE it.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);



}
