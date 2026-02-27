package it.ey.piao.api.repository.mongo;

import it.ey.entity.Attivita;
import it.ey.entity.Attore;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAttivitaRepository extends BaseMongoRepository<Attivita, String> {
}
