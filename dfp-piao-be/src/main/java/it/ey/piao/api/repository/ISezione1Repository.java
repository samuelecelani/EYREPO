package it.ey.piao.api.repository;

import it.ey.entity.Sezione1;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ISezione1Repository extends BaseRepository<Sezione1,Long> {

    @Query("SELECT s FROM Sezione1 s WHERE s.piao.id = :idPiao")
    Sezione1 findByIdPiao(@Param("idPiao") Long idPiao);



    @Query("""
    SELECT s.piao.id
    FROM Sezione1 s
    JOIN s.prioritaPolitiche pp
    WHERE pp.id = :idPriorita
""")
    Optional<Long> findIdPiaoByPrioritaPoliticaId(@Param("idPriorita") Long idPriorita);

    @Query("""
    SELECT s.piao.id
    FROM Sezione1 s
    JOIN s.areeOrganizzative area
    WHERE area.id = :idPriorita
""")
    Optional<Long> findIdPiaoByAreaOrganizzativaId(@Param("idPriorita") Long idPriorita);

    @Modifying
    @Query("""
    UPDATE Sezione1 s SET s.idStato = :idStato,
                          s.updatedByNameSurname = :userNameSurname,
                          s.updatedByRole = :userRole
    WHERE s.piao.id = :idPiao
""")
    void updateStatoSezione(@Param("idPiao") Long idPiao,
                            @Param("idStato") Long idStato,
                            @Param("userNameSurname") String userNameSurname,
                            @Param("userRole") String userRole);
}
