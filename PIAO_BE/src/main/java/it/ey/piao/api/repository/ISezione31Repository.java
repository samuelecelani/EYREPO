package it.ey.piao.api.repository;

import it.ey.entity.Sezione31;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISezione31Repository extends BaseRepository<Sezione31, Long>
{
    @Query("SELECT s FROM Sezione31 s WHERE s.piao.id = :idPiao")
    Sezione31 findByIdPiao(@Param("idPiao") Long idPiao);
}
