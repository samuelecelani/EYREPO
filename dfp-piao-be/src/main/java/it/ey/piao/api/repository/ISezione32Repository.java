package it.ey.piao.api.repository;

import it.ey.entity.Sezione32;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISezione32Repository extends BaseRepository<Sezione32, Long>
{
    @Query("SELECT s FROM Sezione32 s WHERE s.piao.id = :idPiao")
    Sezione32 findByIdPiao(@Param("idPiao") Long idPiao);

    @Modifying
    @Query("""
    UPDATE Sezione32 s SET s.idStato = :idStato,
                          s.updatedByNameSurname = :userNameSurname,
                          s.updatedByRole = :userRole
    WHERE s.piao.id = :idPiao
""")
    void updateStatoSezione(@Param("idPiao") Long idPiao,
                            @Param("idStato") Long idStato,
                            @Param("userNameSurname") String userNameSurname,
                            @Param("userRole") String userRole);
}
