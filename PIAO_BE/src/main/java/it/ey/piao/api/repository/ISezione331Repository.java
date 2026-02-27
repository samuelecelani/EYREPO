package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione331;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface ISezione331Repository extends BaseRepository<Sezione331, Long> {

    public Sezione331 findByPiao(Piao piao);

    @Query("SELECT s FROM Sezione331 s WHERE s.piao.id = :idPiao")
    Sezione331 findByIdPiao(@Param("idPiao") Long idPiao);

}
