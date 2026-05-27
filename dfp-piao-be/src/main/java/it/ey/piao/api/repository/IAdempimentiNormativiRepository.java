package it.ey.piao.api.repository;

import it.ey.entity.AdempimentiNormativi;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAdempimentiNormativiRepository extends BaseRepository<AdempimentiNormativi, Long>
{
    @Query("SELECT a FROM AdempimentiNormativi a WHERE a.sezione23.id = :idSezione23")
    List<AdempimentiNormativi> getAdempimentiNormativiByIdSezione23(@Param("idSezione23") Long idSezione23);

    @Modifying
    @Query("UPDATE AdempimentiNormativi a SET a.active = false, a.deactivationTime = :deactivationTime WHERE a.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
