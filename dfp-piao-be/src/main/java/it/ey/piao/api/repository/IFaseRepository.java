package it.ey.piao.api.repository;

import it.ey.entity.Fase;
import it.ey.entity.Sezione22;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IFaseRepository extends BaseRepository<Fase, Long> {

    @Query("SELECT f FROM Fase f WHERE f.sezione22.id = :idSezione22")
    public List<Fase> getFaseByIdSezione22(@Param("idSezione22") Long idSezione22) ;

    @Modifying
    @Query("UPDATE Fase f SET f.active = false, f.deactivationTime = :deactivationTime WHERE f.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
