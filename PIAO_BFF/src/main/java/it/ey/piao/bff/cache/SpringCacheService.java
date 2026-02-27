package it.ey.piao.bff.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class SpringCacheService {

    private final CacheManager cacheManager;

    public SpringCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public <K, V> void put(String cacheName, K key, V value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    public <K, V> V get(String cacheName, K key, Class<V> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            return cache.get(key, type);
        }
        return null;
    }

    public <K> void delete(String cacheName, K key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
