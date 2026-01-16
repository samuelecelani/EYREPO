package it.ey.piao.api.repository.mongo;

import it.ey.entity.SwotPuntiDebolezza;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISwotPuntiDebolezzaRepository extends BaseMongoRepository<SwotPuntiDebolezza, String> {
}
