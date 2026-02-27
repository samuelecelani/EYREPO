package it.ey.piao.api.utilsPrivate;


import it.ey.entity.BaseEntityMongo;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


@Component

public class MongoUtils {

    private final GenericMapper genericMapper;
    private final ApplicationEventPublisher eventPublisher;

    public MongoUtils(GenericMapper genericMapper, ApplicationEventPublisher eventPublisher) {
        this.genericMapper = genericMapper;
        this.eventPublisher = eventPublisher;
    }


    public <T extends BaseEntityMongo, E> List<E> saveAllItems(List<T> items,
                                                            BaseMongoRepository<E, String> repository,
                                                            Class<E> entityClass) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<E> previousStates = new ArrayList<>();
        List<E> entities = new ArrayList<>();

        for (T item : items) {
            if (shouldDeleteEntity(item, repository)) {
                continue;
            }
            E previous = findExistingEntity(item, repository);
            if (previous != null) {
                previousStates.add(previous);
            }
            entities.add(genericMapper.map(item, entityClass));
        }

        if (!previousStates.isEmpty()) {
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));
        }

        return entities.isEmpty() ? Collections.emptyList() : repository.saveAll(entities);
    }

    public <T extends BaseEntityMongo , E> List<E> saveAllItems(List<T> items, BaseMongoRepository<E, String> repository, Class<E> entityClass, Consumer<E> customizer) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<E> previousStates = new ArrayList<>();
        List<E> entities = new ArrayList<>();

        for (T item : items) {
            if (shouldDeleteEntity(item, repository)) {
                continue;
            }
            E previous = findExistingEntity(item, repository);
            if (previous != null) {
                previousStates.add(previous);
            }
            E entity = genericMapper.map(item, entityClass);
            customizer.accept(entity);
            entities.add(entity);
        }

        if (!previousStates.isEmpty()) {
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));
        }

        return entities.isEmpty() ? Collections.emptyList() : repository.saveAll(entities);
    }

    public <T extends BaseEntityMongo, E> E saveItem(
        T item,
        BaseMongoRepository<E, String> repository,
        Class<E> entityClass,
        Consumer<E> customizer
    ) {
        if (item == null || shouldDeleteEntity(item, repository)) {
            return null;
        }

        E entity = genericMapper.map(item, entityClass);
        E previous = findExistingEntity(item, repository);
        List<E> previousStates = new ArrayList<>();

        if (previous != null) {
            previousStates.add(previous);
        }

        if (customizer != null) {
            customizer.accept(entity);
        }

        if (!previousStates.isEmpty()) {
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));
        }

        return repository.save(entity);
    }

    public <T extends BaseEntityMongo, E> E saveAllItems(
        T item,
        BaseMongoRepository<E, String> repository,
        Class<E> entityClass
    ) {
        if (item == null || shouldDeleteEntity(item, repository)) {
            return null;
        }

        E previous = findExistingEntity(item, repository);
        E entity = genericMapper.map(item, entityClass);

        if (previous != null) {
            List<E> previousStates = new ArrayList<>(1);
            previousStates.add(previous);
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));
        }

        return repository.save(entity);
    }

    private <T extends BaseEntityMongo, E> boolean shouldDeleteEntity(T item, BaseMongoRepository<E, String> repository) {
        if (item == null) {
            return false;
        }
        if (hasEmptyProperties(item)) {
            deleteByExternalId(item, repository);
            return true;
        }
        return false;
    }

    private boolean hasEmptyProperties(BaseEntityMongo entity) {
        if (entity == null) {
            return false;
        }
        List<?> properties = entity.getProperties();
        if (properties == null || properties.isEmpty()) {
            return true;
        }
        return properties.stream().anyMatch(this::hasBlankKeyOrValue);
    }

    private boolean hasBlankKeyOrValue(Object property) {
        if (property == null) {
            return true;
        }
        String key = invokeStringGetter(property, "getKey");
        String value = invokeStringGetter(property, "getValue");
        // Ritorna true se almeno uno tra key o value è blank/null
        return isBlank(key) || isBlank(value);
    }

    private String invokeStringGetter(Object target, String methodName) {
        var method = ReflectionUtils.findMethod(target.getClass(), methodName);
        Object value = method != null ? ReflectionUtils.invokeMethod(method, target) : null;
        return value != null ? value.toString() : null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private <E> void deleteByExternalId(BaseEntityMongo entity, BaseMongoRepository<E, String> repository) {
        if (entity != null && entity.getExternalId() != null) {
            repository.deleteByExternalId(entity.getExternalId());
        }
    }

    private <E> E findExistingEntity(BaseEntityMongo entity, BaseMongoRepository<E, String> repository) {
        return entity != null && entity.getId() != null
            ? repository.findById(entity.getId()).orElse(null)
            : null;
    }



}
