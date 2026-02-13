package it.ey.piao.api.repository.mongo;

import it.ey.entity.SwotMinacce;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISwotMinacceRepository extends BaseMongoRepository<SwotMinacce, String> {
}
