package it.ey.piao.api.repository.mongo;

import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUlterioriInfoRepository extends BaseMongoRepository<UlterioriInfo, String> {

    UlterioriInfo findByExternalIdAndTipoSezione(Long externalId, Sezione sezione);

    void deleteByExternalIdAndTipoSezione(Long externalId, Sezione sezione);

    List<UlterioriInfo> findByExternalIdInAndTipoSezione(List<Long> externalIds, Sezione sezione);

    List<UlterioriInfo> findByExternalIdInAndTipoSezioneIn(List<Long> externalIds, List<Sezione> sezioni);
}
