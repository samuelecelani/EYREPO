package it.ey.piao.bff.cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class SpringCacheService {

    private static final Logger log = LoggerFactory.getLogger(SpringCacheService.class);
    private final CacheManager cacheManager;

    public SpringCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public <K, V> void put(String cacheName, K key, V value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.info("CACHE PUT - cacheName: {}, key: {}, value type: {}",
                cacheName, key, value != null ? value.getClass().getSimpleName() : "null");
        } else {
            log.warn("CACHE PUT FAILED - cache '{}' not found!", cacheName);
        }
    }

    public <K, V> V get(String cacheName, K key, Class<V> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            V value = cache.get(key, type);
            log.info("CACHE GET - cacheName: {}, key: {}, found: {}",
                cacheName, key, value != null);
            return value;
        }
        log.warn("CACHE GET FAILED - cache '{}' not found!", cacheName);
        return null;
    }

    public <K> void delete(String cacheName, K key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("CACHE DELETE - cacheName: {}, key: {}", cacheName, key);
        }
    }

    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("CACHE CLEAR - cacheName: {}", cacheName);
        }
    }
}
