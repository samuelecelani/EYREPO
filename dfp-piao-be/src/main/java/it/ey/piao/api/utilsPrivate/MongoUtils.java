package it.ey.piao.api.utilsPrivate;


import it.ey.entity.BaseEntityMongo;
import it.ey.entity.Property;
import it.ey.piao.api.configuration.MongoPropertyFilterListener;
import it.ey.piao.api.configuration.mapper.GenericMapper;
import it.ey.piao.api.service.event.BeforeUpdateEvent;
import it.ey.repository.mongo.BaseMongoRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Component
public class MongoUtils {

    private static final Logger log = LoggerFactory.getLogger(MongoUtils.class);

    private final GenericMapper genericMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoTemplate mongoTemplate;

    public MongoUtils(GenericMapper genericMapper, ApplicationEventPublisher eventPublisher, MongoTemplate mongoTemplate) {
        this.genericMapper = genericMapper;
        this.eventPublisher = eventPublisher;
        this.mongoTemplate = mongoTemplate;
    }

    // ==================== SAVE ITEM (SINGOLO) ====================

    /**
     * Salva un singolo item MongoDB.
     * Se item è null → il documento viene salvato con tutte le property soft-deleted (active=false).
     * Se item ha properties → le property mancanti rispetto al DB diventano active=false (orphan removal).
     */
    public <T extends BaseEntityMongo, E> E saveItem(
        T item,
        Long externalId,
        BaseMongoRepository<E, String> repository,
        Class<E> entityClass
    ) {
        return saveItem(item, externalId, repository, entityClass, null, null, null);
    }

    public <T extends BaseEntityMongo, E> E saveItem(
        T item,
        Long externalId,
        BaseMongoRepository<E, String> repository,
        Class<E> entityClass,
        Consumer<E> customizer
    ) {
        return saveItem(item, externalId, repository, entityClass, customizer, null, null);
    }

    /**
     * Salva un singolo item MongoDB con customizer e filtro aggiuntivo (tipoSezione).
     * <p>
     * - Se item è null e esiste un documento in DB → soft-delete di tutte le sue properties.
     * - Se item ha properties → confronta con quelle in DB: le mancanti diventano active=false.
     * - Nuove properties vengono salvate con active=true.
     */
    public <T extends BaseEntityMongo, E> E saveItem(
        T item,
        Long externalId,
        BaseMongoRepository<E, String> repository,
        Class<E> entityClass,
        Consumer<E> customizer,
        String filterFieldName,
        Object filterFieldValue
    ) {
        if (item == null) {
            // Se esiste un documento, soft-delete tutte le sue properties
            softDeleteAllProperties(externalId, repository, entityClass, filterFieldName, filterFieldValue);
            return null;
        }

        String collectionName = mongoTemplate.getCollectionName(entityClass);

        // ── STEP 1: Recupera _id e properties DIRETTAMENTE da MongoDB (raw BSON, bypassa listener) ──
        String existingId = null;
        List<Property> dbProperties = null;

        Document rawDoc = findRawDocument(item.getId(), externalId, collectionName, filterFieldName, filterFieldValue);
        if (rawDoc != null) {
            existingId = rawDoc.getObjectId("_id") != null ? rawDoc.getObjectId("_id").toHexString() : rawDoc.getString("_id");
            dbProperties = extractPropertiesFromRaw(rawDoc);
            log.info("[STEP1] Documento esistente trovato: _id={}, properties nel DB={} (dettaglio: {})",
                existingId,
                dbProperties != null ? dbProperties.size() : 0,
                dbProperties != null ? dbProperties.stream().map(p -> p.getKey() + "[active=" + p.getActive() + "]").collect(Collectors.joining(", ")) : "null");
        } else {
            log.info("[STEP1] Nessun documento esistente trovato per externalId={}, filterField={}={}", externalId, filterFieldName, filterFieldValue);
        }

        // Se non ha _id, assegnalo dal documento esistente
        if (item.getId() == null && existingId != null) {
            item.setId(existingId);
            log.info("[STEP1] Assegnato _id={} all'item", existingId);
        }

        // ── STEP 2: Segna properties in arrivo come active=true ──
        ensurePropertiesActive(item);

        List<Property> incomingProperties = item.getProperties() != null ? item.getProperties() : new ArrayList<>();
        log.info("[STEP2] Properties in arrivo (active=true): {}",
            incomingProperties.stream().map(p -> p.getKey() + "=" + p.getValue()).collect(Collectors.joining(", ")));

        // ── STEP 3: Merge con soft-delete ──
        List<Property> mergedProperties = mergeProperties(incomingProperties, dbProperties);

        log.info("[STEP3] Properties dopo merge: {}",
            mergedProperties.stream().map(p -> p.getKey() + "[active=" + p.getActive() + "]").collect(Collectors.joining(", ")));

        // Aggiorna item con la lista mergiata
        item.setProperties(mergedProperties);

        // ── STEP 4: Mapping ──
        E entity;
        if (entityClass.isInstance(item)) {
            entity = entityClass.cast(item);
        } else {
            entity = genericMapper.map(item, entityClass);
        }

        // Riapplica SEMPRE mergedProperties e id dopo il mapping
        if (entity instanceof BaseEntityMongo mongoEntity) {
            mongoEntity.setProperties(mergedProperties);
            if (mongoEntity.getId() == null && item.getId() != null) {
                mongoEntity.setId(item.getId());
            }
        }

        // ── STEP 5: Evento pre-update ──
        if (existingId != null) {
            MongoPropertyFilterListener.disableFilter();
            try {
                E previous = repository.findById(existingId).orElse(null);
                if (previous != null) {
                    eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, List.of(previous)));
                }
            } finally {
                MongoPropertyFilterListener.enableFilter();
            }
        }

        if (customizer != null) {
            customizer.accept(entity);
        }

        // Log finale prima del save
        if (entity instanceof BaseEntityMongo mongoEntity) {
            List<Property> finalProps = mongoEntity.getProperties();
            log.info("[STEP5-SAVE] Documento id={}, tipo={}, properties totali={}: [{}]",
                mongoEntity.getId(), entityClass.getSimpleName(),
                finalProps != null ? finalProps.size() : 0,
                finalProps != null ? finalProps.stream().map(p -> p.getKey() + "[active=" + p.getActive() + ",value=" + p.getValue() + "]").collect(Collectors.joining(", ")) : "");
        }

        E saved = repository.save(entity);
        return clearElementByActiveProperties(saved);
    }

    // ==================== SAVE ALL ITEMS (LISTA) ====================

    public <T extends BaseEntityMongo, E> List<E> saveAllItems(
        List<T> items, Long externalId,
        BaseMongoRepository<E, String> repository, Class<E> entityClass
    ) {
        return saveAllItems(items, externalId, repository, entityClass, null, null, null);
    }

    public <T extends BaseEntityMongo, E> List<E> saveAllItems(
        List<T> items, Long externalId,
        BaseMongoRepository<E, String> repository, Class<E> entityClass,
        Consumer<E> customizer
    ) {
        return saveAllItems(items, externalId, repository, entityClass, customizer, null, null);
    }

    public <T extends BaseEntityMongo, E> List<E> saveAllItems(
        List<T> items, Long externalId,
        BaseMongoRepository<E, String> repository, Class<E> entityClass,
        Consumer<E> customizer, String filterFieldName, Object filterFieldValue
    ) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        String collectionName = mongoTemplate.getCollectionName(entityClass);
        List<E> previousStates = new ArrayList<>();
        List<E> entities = new ArrayList<>();

        for (T item : items) {
            Document rawDoc = findRawDocument(item.getId(), externalId, collectionName, filterFieldName, filterFieldValue);
            String existingId = null;
            List<Property> dbProperties = null;

            if (rawDoc != null) {
                existingId = rawDoc.getObjectId("_id") != null ? rawDoc.getObjectId("_id").toHexString() : rawDoc.getString("_id");
                dbProperties = extractPropertiesFromRaw(rawDoc);
            }

            if (item.getId() == null && existingId != null) {
                item.setId(existingId);
            }

            ensurePropertiesActive(item);
            List<Property> incomingProperties = item.getProperties() != null ? item.getProperties() : new ArrayList<>();
            List<Property> mergedProperties = mergeProperties(incomingProperties, dbProperties);
            item.setProperties(mergedProperties);

            if (existingId != null) {
                MongoPropertyFilterListener.disableFilter();
                try {
                    E prev = repository.findById(existingId).orElse(null);
                    if (prev != null) previousStates.add(prev);
                } finally {
                    MongoPropertyFilterListener.enableFilter();
                }
            }

            E entity = genericMapper.map(item, entityClass);
            if (entity instanceof BaseEntityMongo mongoEntity) {
                mongoEntity.setProperties(mergedProperties);
                if (mongoEntity.getId() == null && item.getId() != null) {
                    mongoEntity.setId(item.getId());
                }
            }

            if (customizer != null) customizer.accept(entity);
            entities.add(entity);
        }

        if (!previousStates.isEmpty()) {
            eventPublisher.publishEvent(new BeforeUpdateEvent<>(entityClass, previousStates));
        }
        return entities.isEmpty() ? Collections.emptyList()
            : repository.saveAll(entities).stream()
                .map(this::clearElementByActiveProperties)
                .collect(Collectors.toList());
    }

    // ==================== LETTURA RAW DA MONGODB (BYPASSA LISTENER) ====================

    /**
     * Legge il documento raw BSON direttamente da MongoDB, bypassando COMPLETAMENTE
     * il sistema di conversione Spring Data e il MongoPropertyFilterListener.
     * Questo garantisce che le properties soft-deleted (active=false) siano sempre visibili.
     */
    private Document findRawDocument(String itemId, Long externalId, String collectionName,
                                     String filterFieldName, Object filterFieldValue) {
        // 1. Cerca per _id
        if (itemId != null) {
            Query query = new Query(Criteria.where("_id").is(itemId));
            Document doc = mongoTemplate.findOne(query, Document.class, collectionName);
            if (doc != null) {
                log.debug("[RAW-FIND] Trovato documento per _id={} nella collection={}", itemId, collectionName);
                return doc;
            }
        }

        if (externalId == null) return null;

        // 2. Cerca per externalId + filtro
        Criteria criteria = Criteria.where("externalId").is(externalId);
        if (filterFieldName != null && filterFieldValue != null) {
            // Converti l'enum in stringa per la query raw
            Object filterValue = filterFieldValue instanceof Enum<?> ? ((Enum<?>) filterFieldValue).name() : filterFieldValue;
            criteria = criteria.and(filterFieldName).is(filterValue);
        }

        Document doc = mongoTemplate.findOne(new Query(criteria), Document.class, collectionName);
        if (doc != null) {
            log.debug("[RAW-FIND] Trovato documento per externalId={}, {}={} nella collection={}",
                externalId, filterFieldName, filterFieldValue, collectionName);
        } else {
            log.debug("[RAW-FIND] NON trovato documento per externalId={}, {}={} nella collection={}",
                externalId, filterFieldName, filterFieldValue, collectionName);
        }
        return doc;
    }

    /**
     * Estrae la lista di Property da un documento raw BSON.
     * Legge direttamente i campi key, value, active, deactivationTime dal BSON.
     */
    @SuppressWarnings("unchecked")
    private List<Property> extractPropertiesFromRaw(Document rawDoc) {
        List<Document> rawProps = rawDoc.getList("properties", Document.class);
        if (rawProps == null || rawProps.isEmpty()) {
            return new ArrayList<>();
        }

        List<Property> properties = new ArrayList<>();
        for (Document propDoc : rawProps) {
            Property p = new Property();
            p.setKey(propDoc.getString("key"));
            p.setValue(propDoc.getString("value"));
            p.setActive(propDoc.getBoolean("active", true)); // default true per retrocompatibilità
            if (propDoc.get("deactivationTime") != null) {
                try {
                    Object dt = propDoc.get("deactivationTime");
                    if (dt instanceof Date) {
                        p.setDeactivationTime(((Date) dt).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    }
                } catch (Exception e) {
                    log.warn("[RAW-FIND] Errore parsing deactivationTime per key={}: {}", p.getKey(), e.getMessage());
                }
            }
            properties.add(p);
        }
        return properties;
    }

    // ==================== LOGICA MERGE / SOFT DELETE ====================

    private void ensurePropertiesActive(BaseEntityMongo item) {
        if (item == null) return;
        List<Property> properties = item.getProperties();
        if (properties == null || properties.isEmpty()) return;
        for (Property p : properties) {
            p.setActive(true);
            p.setDeactivationTime(null);
        }
    }

    /**
     * Merge tra properties in arrivo e properties dal DB.
     * <p>
     * Discriminante sulla SIZE delle sole properties ATTIVE nel DB:
     * <ul>
     *   <li>incoming.size == dbActive.size → puro UPDATE → nessuna soft-delete,
     *       preserva solo le già soft-deleted esistenti.</li>
     *   <li>incoming.size < dbActive.size → RIMOZIONE → il frontend ha rimosso delle property
     *       e rinumerato le key. Le prime N property attive del DB corrispondono alle N in arrivo
     *       (aggiornate), le restanti property attive del DB sono state rimosse → soft-delete.</li>
     *   <li>incoming.size > dbActive.size → AGGIUNTA → nessuna soft-delete,
     *       preserva solo le già soft-deleted esistenti.</li>
     * </ul>
     */
    private List<Property> mergeProperties(List<Property> incomingProperties, List<Property> dbProperties) {
        if (dbProperties == null || dbProperties.isEmpty()) {
            return new ArrayList<>(incomingProperties);
        }

        // Separa DB in attive e soft-deleted
        List<Property> dbActive = dbProperties.stream()
            .filter(p -> !Boolean.FALSE.equals(p.getActive()))
            .collect(Collectors.toList());
        List<Property> dbSoftDeleted = dbProperties.stream()
            .filter(p -> Boolean.FALSE.equals(p.getActive()))
            .collect(Collectors.toList());

        int incomingCount = incomingProperties.size();
        int dbActiveCount = dbActive.size();

        log.info("[MERGE] Properties in arrivo={}, properties attive DB={}, già soft-deleted DB={}",
            incomingCount, dbActiveCount, dbSoftDeleted.size());

        // Parti dalle incoming (tutte active=true)
        List<Property> merged = new ArrayList<>(incomingProperties);

        if (incomingCount < dbActiveCount) {
            // ── RIMOZIONE: le property attive DB oltre la posizione incomingCount sono state rimosse ──
            log.info("[MERGE] Size minore → rimozione rilevata, soft-delete per posizione");

            // Le property attive dalla posizione incomingCount in poi sono quelle rimosse
            for (int i = incomingCount; i < dbActiveCount; i++) {
                Property removed = dbActive.get(i);
                removed.setActive(false);
                removed.setDeactivationTime(LocalDateTime.now());
                merged.add(removed);
                log.info("[SOFT-DELETE] Property key='{}', value='{}' (posizione {}) → active=false",
                    removed.getKey(), removed.getValue(), i);
            }
        } else {
            // ── STESSA SIZE (update) o SIZE MAGGIORE (aggiunta) → nessuna soft-delete ──
            log.info("[MERGE] Size {} → {}", incomingCount == dbActiveCount ? "uguale (update)" : "maggiore (aggiunta)",
                "nessuna nuova soft-delete");
        }

        // Preserva SEMPRE le property già soft-deleted
        merged.addAll(dbSoftDeleted);

        return merged;
    }

    /**
     * Soft-delete totale: marca tutte le properties active=false.
     */
    private <E> void softDeleteAllProperties(
        Long externalId, BaseMongoRepository<E, String> repository, Class<E> entityClass,
        String filterFieldName, Object filterFieldValue
    ) {
        if (externalId == null) return;

        String collectionName = mongoTemplate.getCollectionName(entityClass);
        Document rawDoc = findRawDocument(null, externalId, collectionName, filterFieldName, filterFieldValue);

        if (rawDoc == null) return;

        String docId = rawDoc.getObjectId("_id") != null ? rawDoc.getObjectId("_id").toHexString() : rawDoc.getString("_id");
        List<Property> dbProperties = extractPropertiesFromRaw(rawDoc);

        if (dbProperties == null || dbProperties.isEmpty()) return;

        boolean changed = false;
        for (Property p : dbProperties) {
            if (p.getActive() == null || p.getActive()) {
                p.setActive(false);
                p.setDeactivationTime(LocalDateTime.now());
                changed = true;
                log.info("[SOFT-DELETE-ALL] Property key='{}' → active=false per externalId={}", p.getKey(), externalId);
            }
        }

        if (changed && docId != null) {
            MongoPropertyFilterListener.disableFilter();
            try {
                E existing = repository.findById(docId).orElse(null);
                if (existing instanceof BaseEntityMongo mongoEntity) {
                    mongoEntity.setProperties(dbProperties);
                    repository.save(existing);
                }
            } finally {
                MongoPropertyFilterListener.enableFilter();
            }
        }
    }


     // Rimuove dall'entità (in memoria) le Property con active=false, così che la
     //response REST non esponga le proprietà soft-deleted.
    public <E> E clearElementByActiveProperties(E entity) {
        if (entity instanceof BaseEntityMongo base) {
            List<Property> props = base.getProperties();
            if (props != null && !props.isEmpty()) {
                List<Property> activeOnly = props.stream()
                    .filter(p -> p != null && (p.getActive() == null || p.getActive()))
                    .collect(Collectors.toList());
                base.setProperties(activeOnly);
            }
        }
        return entity;
    }
}
