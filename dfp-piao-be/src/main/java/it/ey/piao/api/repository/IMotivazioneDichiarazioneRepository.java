package it.ey.piao.api.repository;

import it.ey.entity.MotivazioneDichiarazione;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IMotivazioneDichiarazioneRepository extends BaseRepository<MotivazioneDichiarazione, Long>
{
    @Modifying
    @Query("UPDATE MotivazioneDichiarazione m SET m.active = false, m.deactivationTime = :deactivationTime WHERE m.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
