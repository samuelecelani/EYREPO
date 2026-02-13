package it.ey.piao.api.repository.mongo;

import it.ey.entity.Logo;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILogoRepository extends BaseMongoRepository<Logo, String> {
}
