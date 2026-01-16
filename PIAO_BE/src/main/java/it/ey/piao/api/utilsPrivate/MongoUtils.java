package it.ey.piao.api.utilsPrivate;


import it.ey.entity.BaseEntityMongo;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.repository.mongo.BaseMongoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
        if (items != null && !items.isEmpty()) {
            List<E> previousStates = new ArrayList<>();
            List<E> entities = items.stream()
                .map(item -> {
                    E previous = findExistingEntity(item, repository);
                    if (previous != null) {
                        previousStates.add(previous);
                    }
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));

                    return genericMapper.map(item, entityClass);
                })
                .toList();
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));
            return repository.saveAll(entities);
        }
    return Collections.emptyList();

    }
    public <T extends BaseEntityMongo , E> List<E> saveAllItems(List<T> items, BaseMongoRepository<E, String> repository, Class<E> entityClass, Consumer<E> customizer) {
        if (items != null && !items.isEmpty()) {
            List<E> previousStates = new ArrayList<>();

            List<E> entities = items.stream()
                .map(item -> {
                    E previous = findExistingEntity(item, repository);
                    if (previous != null) {
                        previousStates.add(previous);
                    }
                    E entity = genericMapper.map(item, entityClass);
                    customizer.accept(entity);

                    return entity;
                })
                .toList();
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));

            return repository.saveAll(entities);
        }
        return Collections.emptyList();

    }


    public <T extends BaseEntityMongo, E> E saveItem(
        T item,
        BaseMongoRepository<E, String> repository,
        Class<E> entityClass,
        Consumer<E> customizer
    ) {
        if (item == null) {
            return null;
        }

        E previous = findExistingEntity(item, repository);
        List<E> previousStates = new ArrayList<>();

        if (previous != null) {
            previousStates.add(previous);
        }

        // Mappo l'entità
        E entity = genericMapper.map(item, entityClass);

        if (customizer != null) {
            customizer.accept(entity);
        }

        // Evento BeforeUpdate
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
        if (item == null) {
            return null;
        }

        // Recupero lo stato precedente, se esiste
        E previous = findExistingEntity(item, repository);

        // Mappo l'entità
        E entity = genericMapper.map(item, entityClass);

        // Pubblico l’evento una sola volta con il previous (se presente)
        if (previous != null) {
            List<E> previousStates = new ArrayList<>(1);
            previousStates.add(previous);
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));
        }

        // Salvo e ritorno l’entità
        return repository.save(entity);
    }


    private <E> E findExistingEntity(BaseEntityMongo entity, BaseMongoRepository<E, String> repository) {
        return entity != null && entity.getId() != null
            ? repository.findById(entity.getId()).orElse(null)
            : null;
    }



}

