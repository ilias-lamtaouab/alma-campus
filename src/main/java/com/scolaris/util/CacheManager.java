package com.scolaris.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    
    private static final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final Map<String, Long> timestamps = new ConcurrentHashMap<>();
    
    // 5 minutes cache validity
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;

    public static void put(String key, Object value) {
        cache.put(key, value);
        timestamps.put(key, System.currentTimeMillis());
    }

    public static Object get(String key) {
        Long timestamp = timestamps.get(key);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < CACHE_TTL_MS) {
            return cache.get(key);
        }
        // Cache expired or not found
        cache.remove(key);
        timestamps.remove(key);
        return null;
    }
    
    public static void invalidate(String key) {
        cache.remove(key);
        timestamps.remove(key);
    }
    
    public static void invalidateAll() {
        cache.clear();
        timestamps.clear();
    }
}
