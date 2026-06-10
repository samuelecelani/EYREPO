package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione23;
import it.ey.entity.Sezione4;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ISezione4Repository extends BaseRepository<Sezione4, Long> {

    @Query("SELECT s FROM Sezione4 s WHERE s.piao.id = :idPiao")
    Sezione4 findByIdPiao(@Param("idPiao") Long idPiao);

    @Modifying
    @Query("""
    UPDATE Sezione4 s SET s.idStato = :idStato,
                          s.updatedByNameSurname = :userNameSurname,
                          s.updatedByRole = :userRole
    WHERE s.piao.id = :idPiao
""")
    void updateStatoSezione(@Param("idPiao") Long idPiao,
                            @Param("idStato") Long idStato,
                            @Param("userNameSurname") String userNameSurname,
                            @Param("userRole") String userRole);
}
