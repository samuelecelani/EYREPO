package it.ey.piao.api.repository;

import it.ey.entity.Adempimento;
import it.ey.enums.TipologiaAdempimento;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IAdempimentoRepository extends BaseRepository<Adempimento, Long>
{
    @Query("SELECT a FROM Adempimento a WHERE a.sezione22.id = :idSezione22 AND a.tipologia IN :tipologia")
    public List<Adempimento> getAdempimentoBySezione22AndTipologia(@Param("idSezione22") Long idSezione22, @Param("tipologia") TipologiaAdempimento tipologia);

    @Modifying
    @Query("UPDATE Adempimento a SET a.active = false, a.deactivationTime = :deactivationTime WHERE a.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
