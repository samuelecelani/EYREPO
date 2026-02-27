package it.ey.piao.api.repository;

import it.ey.entity.FondiEuropei;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFondiEuropeiRepository extends BaseRepository<FondiEuropei,Long> {
}
