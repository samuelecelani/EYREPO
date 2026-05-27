package it.ey.piao.api.repository;

import it.ey.entity.AppLog;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppLogRepository extends BaseRepository<AppLog, Long> {
}

