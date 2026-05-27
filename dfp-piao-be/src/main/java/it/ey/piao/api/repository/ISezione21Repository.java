package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione21;
import it.ey.entity.Sezione331;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISezione21Repository extends BaseRepository<Sezione21,Long> {

    public Sezione21 findByPiao(Piao piao);
    Optional<Sezione21> findByPiaoId(Long piaoId);



    @Query("SELECT s FROM Sezione21 s WHERE s.piao.id = :idPiao")
    Sezione21 findByIdPiao(@Param("idPiao") Long idPiao);

    @Modifying
    @Query("""
    UPDATE Sezione21 s SET s.idStato = :idStato,
                          s.updatedByNameSurname = :userNameSurname,
                          s.updatedByRole = :userRole
    WHERE s.piao.id = :idPiao
""")
    void updateStatoSezione(@Param("idPiao") Long idPiao,
                            @Param("idStato") Long idStato,
                            @Param("userNameSurname") String userNameSurname,
                            @Param("userRole") String userRole);

}
