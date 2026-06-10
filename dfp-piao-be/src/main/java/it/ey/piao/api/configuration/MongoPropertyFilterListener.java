package it.ey.piao.api.configuration;

import it.ey.entity.BaseEntityMongo;
import it.ey.entity.Property;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Listener globale MongoDB: dopo ogni conversione da documento MongoDB a entità Java,
 * filtra automaticamente le properties con active=false (soft-deleted).
 * <p>
 * Questo listener copre TUTTI i metodi di tutti i repository MongoDB
 * (findById, findAll, derived queries come findByExternalIdAndTipoSezione, ecc.)
 */
@Component
public class MongoPropertyFilterListener extends AbstractMongoEventListener<BaseEntityMongo> {

    private static final ThreadLocal<Boolean> skipFilter = ThreadLocal.withInitial(() -> false);

    /**
     * Disabilita temporaneamente il filtro sulle property inactive.
     * Usare sempre in un blocco try-finally con {@link #enableFilter()}.
     */
    public static void disableFilter() {
        skipFilter.set(true);
    }

    /**
     * Riabilita il filtro sulle property inactive.
     */
    public static void enableFilter() {
        skipFilter.set(false);
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<BaseEntityMongo> event) {
        if (Boolean.TRUE.equals(skipFilter.get())) return;

        BaseEntityMongo entity = event.getSource();
        if (entity == null) return;

        List<Property> properties = entity.getProperties();
        if (properties != null && !properties.isEmpty()) {
            List<Property> activeOnly = properties.stream()
                .filter(p -> p.getActive() == null || p.getActive())
                .collect(Collectors.toList());
            entity.setProperties(activeOnly);
        }
    }
}

