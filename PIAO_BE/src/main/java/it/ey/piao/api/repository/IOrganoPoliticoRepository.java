package it.ey.piao.api.repository;

import it.ey.entity.OrganoPolitico;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOrganoPoliticoRepository extends BaseRepository<OrganoPolitico, Long> {
}
