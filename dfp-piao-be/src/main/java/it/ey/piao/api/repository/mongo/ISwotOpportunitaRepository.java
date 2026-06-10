package it.ey.piao.api.repository.mongo;

import it.ey.entity.SwotOpportunita;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISwotOpportunitaRepository extends BaseMongoRepository<SwotOpportunita, String> {
}
