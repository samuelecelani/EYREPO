package it.ey.piao.api.repository;

import it.ey.entity.StakeHolder;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IStakeHolderRepository extends BaseRepository<StakeHolder, Long> {

    @Query("SELECT s FROM StakeHolder s WHERE s.piao.id = :idPiao")
    List<StakeHolder> findByIdPiao(@Param("idPiao") Long idPiao);
}
