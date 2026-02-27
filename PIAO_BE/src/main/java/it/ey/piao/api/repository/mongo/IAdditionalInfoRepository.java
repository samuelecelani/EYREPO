package it.ey.piao.api.repository.mongo;

import it.ey.entity.AdditionalInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAdditionalInfoRepository extends MongoRepository<AdditionalInfo, String> {
    public AdditionalInfo findByExternalId(Long externalId);

}
