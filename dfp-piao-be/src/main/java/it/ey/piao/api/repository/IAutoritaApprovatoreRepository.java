package it.ey.piao.api.repository;

import it.ey.entity.AutoritaApprovatore;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAutoritaApprovatoreRepository extends BaseRepository<AutoritaApprovatore, Long> {}
