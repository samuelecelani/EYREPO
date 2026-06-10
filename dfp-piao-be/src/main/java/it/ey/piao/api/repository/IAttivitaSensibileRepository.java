package it.ey.piao.api.repository;

import it.ey.entity.AttivitaSensibile;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IAttivitaSensibileRepository  extends BaseRepository <AttivitaSensibile,Long> {

    @Query("SELECT a FROM AttivitaSensibile a WHERE a.sezione23.id = :idSezione23")
    List<AttivitaSensibile> getAttivitaSensibileByIdSezione23(@Param("idSezione23") Long idSezione23);

    @Modifying
    @Query("UPDATE AttivitaSensibile a SET a.active = false, a.deactivationTime = :deactivationTime WHERE a.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
