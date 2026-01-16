package it.ey.piao.api.repository;

import it.ey.entity.PrincipioGuida;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPrincipioGuidaRepository extends BaseRepository<PrincipioGuida, Long> {
}
