package it.ey.piao.api.repository.mongo;

import it.ey.entity.SwotPuntiForza;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISwotPuntiForzaRepository extends BaseMongoRepository<SwotPuntiForza, String> {
}
