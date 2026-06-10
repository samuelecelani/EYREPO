package it.ey.repository.mongo;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;


@NoRepositoryBean
public interface BaseMongoRepository<T, ID> extends MongoRepository<T, ID> {
    T getByExternalId(Long externalId);
    void deleteByExternalId(Long externalId);
}
