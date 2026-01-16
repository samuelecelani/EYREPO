package it.ey.piao.api.repository.mongo;

import it.ey.entity.Social;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISocialRepository extends BaseMongoRepository<Social, String> {
}
