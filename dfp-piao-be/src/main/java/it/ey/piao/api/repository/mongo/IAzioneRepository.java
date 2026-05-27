package it.ey.piao.api.repository.mongo;

import it.ey.entity.Azione;
import it.ey.enums.Sezione;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAzioneRepository extends BaseMongoRepository<Azione, String> { }
