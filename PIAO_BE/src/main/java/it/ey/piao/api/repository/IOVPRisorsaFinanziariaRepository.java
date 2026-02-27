package it.ey.piao.api.repository;

import it.ey.entity.OVPRisorsaFinanziaria;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOVPRisorsaFinanziariaRepository extends BaseRepository<OVPRisorsaFinanziaria, Long> {
}
