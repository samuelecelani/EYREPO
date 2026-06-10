package it.ey.piao.api.repository;

import it.ey.entity.Avviso;
import it.ey.enums.StatoAvviso;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IAvvisoRepository extends BaseRepository<Avviso, Long> {
    List<Avviso> findAllByStato(StatoAvviso stato);

    @Modifying
    @Query("UPDATE Avviso a SET a.active = false, a.deactivationTime = :deactivationTime WHERE a.id = :id")
    int softDeleteById(@Param("id") Long id, @Param("deactivationTime") LocalDateTime deactivationTime);
}
