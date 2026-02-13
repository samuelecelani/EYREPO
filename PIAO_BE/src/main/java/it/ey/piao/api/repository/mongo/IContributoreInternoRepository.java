package it.ey.piao.api.repository.mongo;

import it.ey.entity.ContributoreInterno;
import it.ey.repository.mongo.BaseMongoRepository;
import it.ey.repository.mongo.BaseMongoRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public interface IContributoreInternoRepository  extends BaseMongoRepository<ContributoreInterno, String> {
}
