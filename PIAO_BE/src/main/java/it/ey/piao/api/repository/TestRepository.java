package it.ey.piao.api.repository;
import it.ey.entity.Test;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends BaseRepository<Test, Long> {



}
