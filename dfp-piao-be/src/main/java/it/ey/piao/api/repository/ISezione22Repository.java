package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione21;
import it.ey.entity.Sezione22;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISezione22Repository extends BaseRepository<Sezione22,Long> {

    public Sezione22 findByPiao(Piao piao);
    Optional<Sezione22> findByPiaoId(Long piaoId);

    @Query("SELECT s FROM Sezione22 s WHERE s.piao.id = :idPiao")
    Sezione22 findByIdPiao(@Param("idPiao") Long idPiao);

    @Query("""
    SELECT s.piao.id
    FROM Sezione22 s
    JOIN s.obbiettiviPerformance op
    WHERE op.id = :idObbPerf
""")
    Optional<Long> findIdPiaoByObiettivoPerformanceId(@Param("idObbPerf") Long idObbPerf);

    @Modifying
    @Query("""
    UPDATE Sezione22 s SET s.idStato = :idStato,
                          s.updatedByNameSurname = :userNameSurname,
                          s.updatedByRole = :userRole
    WHERE s.piao.id = :idPiao
""")
    void updateStatoSezione(@Param("idPiao") Long idPiao,
                            @Param("idStato") Long idStato,
                            @Param("userNameSurname") String userNameSurname,
                            @Param("userRole") String userRole);

}
