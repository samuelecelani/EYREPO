package it.ey.piao.api.repository;

import it.ey.entity.OVP;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OVPRepository extends BaseRepository<OVP, Long> {

    List<OVP> findBySezione21Id(Long idSezione21);

}

