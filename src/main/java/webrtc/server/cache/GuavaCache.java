package webrtc.server.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class GuavaCache {

    private Cache<String, Object> cache;

    public GuavaCache() {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.HOURS)
                .build();
    }

    public void add(String key, Object value) {
        cache.put(key, value);
    }


    public void add(String key, String group, Object value) {
        cache.put(key, value);

        List<String> keys = null;

        keys = (List<String>) cache.getIfPresent(group);
        if (keys == null) {
            keys = new ArrayList<>();
        }
        keys.add(key);
        cache.put(group, keys);
    }


    public Object get(String key) {
        return cache.getIfPresent(key);
    }

    public Collection<Object> getValues() {
        return cache.asMap().values();
    }

    public Collection<String> getKeys() {
        return cache.asMap().keySet();
    }

    public Collection<String> getKeys(String group) {
        return (List<String>) cache.getIfPresent(group);
    }

    public void remove(String key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
    }

    public void clear(String group) {
        List<String> keys = null;

        keys = (List<String>) cache.getIfPresent(group);
        if (keys != null)
            cache.invalidateAll(keys);
    }
}
