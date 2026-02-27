package it.ey.piao.api.repository.mongo;

import it.ey.entity.Attore;
import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAttoreRepository extends BaseMongoRepository<Attore, String> {
    Attore findAllByExternalIdAndTipoSezione(Long externalId, Sezione sezione);
    Attore findByExternalIdOrIdPiaoAndTipoSezione(Long externalId, Long idPiao,Sezione sezione);
    List<Attore> findByIdPiaoAndTipoSezione(Long idPiao, Sezione sezione);
    void deleteByExternalIdAndTipoSezione(Long externalId, Sezione sezione);
}
