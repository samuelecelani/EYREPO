package it.ey.piao.api.repository;

import it.ey.entity.Milestone;
import it.ey.entity.Promemoria;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPromemoriaRepository extends BaseRepository<Promemoria, Long> {}
