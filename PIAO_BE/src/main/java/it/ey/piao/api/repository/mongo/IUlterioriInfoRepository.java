package it.ey.piao.api.repository.mongo;

import it.ey.entity.UlterioriInfo;
import it.ey.enums.Sezione;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUlterioriInfoRepository extends BaseMongoRepository<UlterioriInfo, String> {
    public UlterioriInfo findByExternalIdAndTipoSezione(Long externalId, Sezione sezione);
    public void deleteByExternalIdAndTipoSezione(Long externalId, Sezione sezione);

    /**
     * Carica in batch tutti gli UlterioriInfo per una lista di externalId
     * @param externalIds lista di ID esterni
     * @param sezione tipo di sezione
     * @return lista di UlterioriInfo
     */
    List<UlterioriInfo> findByExternalIdInAndTipoSezione(List<Long> externalIds, Sezione sezione);
}
