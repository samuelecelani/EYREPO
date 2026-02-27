package it.ey.repository;

import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;

import java.util.List;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class BaseRepositoryImpl<T, ID> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    private final EntityManager entityManager;
    private final Class<T> entityClass;


    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityClass = entityInformation.getJavaType();
    }



    @SuppressWarnings("unchecked")
    @Override
    public List<T> getStoricoById(ID id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return (List<T>) auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, true, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();
    }

    @Override
    public T merge(T entity) {
        return entityManager.merge(entity);
    }

}

