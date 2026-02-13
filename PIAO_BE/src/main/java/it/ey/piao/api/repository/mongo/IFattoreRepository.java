package it.ey.piao.api.repository.mongo;

import it.ey.entity.Fattore;
import it.ey.enums.Sezione;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFattoreRepository extends BaseMongoRepository<Fattore, String> {
    Fattore findByExternalIdAndTipoSezione(Long externalId, Sezione sezione);
    void deleteByExternalIdAndTipoSezione(Long externalId, Sezione sezione);
}
