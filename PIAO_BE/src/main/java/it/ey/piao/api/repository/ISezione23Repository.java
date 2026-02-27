package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione1;
import it.ey.entity.Sezione22;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISezione23Repository extends BaseRepository<Sezione23,Long> {

    @Query("SELECT s FROM Sezione23 s WHERE s.piao.id = :idPiao")
    Sezione23 findByIdPiao(@Param("idPiao") Long idPiao);
}
