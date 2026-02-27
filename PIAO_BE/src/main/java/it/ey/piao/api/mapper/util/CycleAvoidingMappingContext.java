package it.ey.piao.api.mapper.util;

import org.mapstruct.BeforeMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.TargetType;

/**
 * Context per MapStruct che previene i cicli infiniti durante il mapping di entità con relazioni circolari.
 *
 * Usa IdentityHashMap per tracciare gli oggetti già mappati basandosi sull'identità dell'oggetto (non equals/hashCode).
 * Questo previene il StackOverflowError quando MapStruct mapperebbe ripetutamente le stesse istanze.
 *
 * Uso:
 * - Aggiungere @Context CycleAvoidingMappingContext context come parametro nei metodi mapper
 * - MapStruct userà automaticamente questo context per tutti i mapping annidati
 */
public class CycleAvoidingMappingContext {

    private final java.util.Map<Object, Object> knownInstances = new java.util.IdentityHashMap<>();

    /**
     * Chiamato automaticamente da MapStruct PRIMA di ogni mapping.
     * Se l'oggetto source è già stato mappato, restituisce l'istanza cached invece di rimappare.
     */
    @BeforeMapping
    public <T> T getMappedInstance(Object source, @TargetType Class<T> targetType) {
        @SuppressWarnings("unchecked")
        T mappedInstance = (T) knownInstances.get(source);
        return mappedInstance;
    }

    /**
     * Chiamato automaticamente da MapStruct DOPO ogni mapping.
     * Salva l'associazione source → target per prevenire rimappature future.
     */
    @BeforeMapping
    public void storeMappedInstance(Object source, @MappingTarget Object target) {
        knownInstances.put(source, target);
    }
}
