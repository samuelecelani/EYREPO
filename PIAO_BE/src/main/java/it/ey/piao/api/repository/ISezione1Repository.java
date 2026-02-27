package it.ey.piao.api.repository;

import it.ey.entity.Sezione1;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ISezione1Repository extends BaseRepository<Sezione1,Long> {

    @Query("SELECT s FROM Sezione1 s WHERE s.piao.id = :idPiao")
    Sezione1 findByIdPiao(@Param("idPiao") Long idPiao);
}
