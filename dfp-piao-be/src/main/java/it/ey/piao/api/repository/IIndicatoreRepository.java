package it.ey.piao.api.repository;

import it.ey.entity.Indicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IIndicatoreRepository extends BaseRepository<Indicatore, Long> {

    @Query("SELECT i FROM Indicatore i WHERE i.piao.id = :piaoId AND i.codTipologiaFK IN :codTipologiaFK")
    List<Indicatore> findByPiaoIdAndCodTipologiaFK(@Param("piaoId") Long piaoId,
                                                   @Param("codTipologiaFK") String codTipologiaFK);


    @Modifying
    @Query("""
    UPDATE Indicatore i
    SET i.active = false,
        i.deactivationTime = :deactivationTime
    WHERE i.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);


}
