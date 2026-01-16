package it.ey.piao.api.repository;

import it.ey.entity.IntegrationTeam;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IIntegrationTeamRepository extends BaseRepository<IntegrationTeam, Long> {
}
