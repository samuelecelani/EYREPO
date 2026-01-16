package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.StakeHolder;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IStakeHolderRepository extends BaseRepository<StakeHolder, Long> {

    public List<StakeHolder> findByPiao(Piao piao);
}
