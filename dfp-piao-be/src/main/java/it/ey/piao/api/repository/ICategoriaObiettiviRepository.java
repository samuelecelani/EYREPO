package it.ey.piao.api.repository;

import it.ey.entity.CategoriaObiettivi;
import it.ey.entity.Sezione4;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ICategoriaObiettiviRepository extends BaseRepository<CategoriaObiettivi, Long> {

    @Query("SELECT c FROM CategoriaObiettivi c WHERE c.sezione4.id = :idSezione4")
    List<CategoriaObiettivi> getCategoriaObiettiviByIdSezione4(@Param("idSezione4") Long idSezione4);

    @Modifying
    @Query("UPDATE CategoriaObiettivi c SET c.active = false, c.deactivationTime = :deactivationTime WHERE c.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("UPDATE CategoriaObiettivi c SET c.active = false, c.deactivationTime = :deactivationTime WHERE c.sottofase.id = :idSottofase")
    void softDeleteBySottofaseId(@Param("idSottofase") Long idSottofase,
                        @Param("deactivationTime") LocalDateTime deactivationTime);


}
