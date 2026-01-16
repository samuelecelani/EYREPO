package it.ey.repository.mongo;


import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.query.MongoEntityInformation;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;

public class BaseMongoRepositoryImpl<T, ID> extends SimpleMongoRepository<T, ID> implements BaseMongoRepository<T, ID> {

    private final MongoOperations mongoOperations;
    private final Class<T> entityClass;


    public BaseMongoRepositoryImpl(MongoEntityInformation<T, ID> entityInformation,
                                   MongoOperations mongoOperations) {
        super(entityInformation, mongoOperations);
        this.mongoOperations = mongoOperations;
        this.entityClass = entityInformation.getJavaType();
    }

    @Override
    public T getByExternalId(Long externalId) {
        Query query = new Query(Criteria.where("externalId").is(externalId));
        return mongoOperations.findOne(query, entityClass);
    }

    @Override
    public void deleteByExternalId(Long externalId) {
        Query query = new Query(Criteria.where("externalId").is(externalId));
        mongoOperations.remove(query, entityClass);
    }

}




