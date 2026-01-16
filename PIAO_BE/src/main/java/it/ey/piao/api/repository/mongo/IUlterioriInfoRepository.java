package it.ey.piao.api.repository.mongo;

import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUlterioriInfoRepository extends BaseMongoRepository<UlterioriInfo, String> {
    public UlterioriInfo findByExternalIdAndTipoSezione(Long externalId, Sezione sezione);
}
