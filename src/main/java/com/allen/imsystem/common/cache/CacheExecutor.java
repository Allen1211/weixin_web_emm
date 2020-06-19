package com.allen.imsystem.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName CacheExecutor
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/17
 * @Version 1.0
 */
@Slf4j
public class CacheExecutor {

    public static <K, V> V get(KeyValueCache<K, V> keyValueCache, K key) {
        V val = null;
        try {
            val = keyValueCache.get(key);
        } catch (Exception e) {
            log.error("缓存执行(get)失败：" + e.getMessage());
            return keyValueCache.onFail(key);
        }
        if (val == null) {
            return keyValueCache.onMiss(key);
        }
        return val;
    }

    public static <K, V> Set<V> get(SetCache<K, V> setCache, K key) {
        Set<V> vals = null;
        try {
            vals = setCache.get(key);
        } catch (Exception e) {
            log.error("缓存执行(get)失败：" + e.getMessage());
            return setCache.onFail(key);
        }
        if (CollectionUtils.isEmpty(vals)) {
            return setCache.onMiss(key);
        }
        return vals;
    }

    public static <K, F, V> V get(HashCache<K, F, V> cache, K key, F field) {
        V val = null;
        try {
            val = cache.get(key, field);
        } catch (Exception e) {
            log.error("缓存执行(get)失败：" + e.getMessage());
            return cache.onFail(key, field);
        }
        if (val == null) {
            return cache.onMiss(key, field);
        }
        return val;
    }

    public static <K, V> void set(KeyValueCache<K, V> keyValueCache, K key, V val) {
        try {
            keyValueCache.set(key, val);
        } catch (Exception e) {
            log.error("缓存执行(set)失败：" + e.getMessage());
        }
    }

    public static <K, V> void set(SetCache<K, V> setCache, K key, Set<V> vals) {
        try {
            setCache.set(key, vals);
        } catch (Exception e) {
            log.error("缓存执行(set)失败：" + e.getMessage());
        }
    }
    public static <K, F, V> void set(HashCache<K, F, V> cache, K key, F field, V val) {
        try {
            cache.set(key, field, val);
        } catch (Exception e) {
            log.error("缓存执行(set)失败：" + e.getMessage());
        }
    }

    public static <K, V> void add(SetCache<K, V> setCache, K key, V val) {
        try {
            setCache.add(key, val);
        } catch (Exception e) {
            log.error("缓存执行(add)失败：" + e.getMessage());
        }
    }

    public static <K, V> void addIfExist(SetCache<K, V> setCache, K key, V val) {
        try {
            if(exist(setCache,key)){
                setCache.add(key, val);
            }
        } catch (Exception e) {
            log.error("缓存执行(add)失败：" + e.getMessage());
        }
    }

    public static <K, V> void remove(Cache<K> cache, K key) {
        try {
            cache.remove(key);
        } catch (Exception e) {
            log.error("缓存执行(remove)失败：" + e.getMessage());
        }
    }

    public static <K, V> void remove(SetCache<K, V> setCache, K key, V val) {
        try {
            setCache.remove(key,val);
        } catch (Exception e) {
            log.error("缓存执行(remove)失败：" + e.getMessage());
        }
    }

    public static <K, F, V> void remove(HashCache<K, F, V> cache, K key, F field) {
        try {
            cache.remove(key, field);
        } catch (Exception e) {
            log.error("缓存执行(remove)失败：" + e.getMessage());
        }
    }

    public static <K, V> V incr(KeyValueCache<K, V> keyValueCache, K key, V delta) {
        try {
            return keyValueCache.incr(key, delta);
        } catch (Exception e) {
            log.error("缓存执行(incr)失败：" + e.getMessage());
            return null;
        }
    }

    public static <K, F, V> V incr(HashCache<K, F, V> cache, K key, F field, V delta) {
        try {
            return cache.incr(key, field, delta);
        } catch (Exception e) {
            log.error("缓存执行(incr)失败：" + e.getMessage());
            return null;
        }
    }


    public static <K, F, V> boolean exist(Cache<K> cache, K key) {
        try {
            return cache.exist(key);
        } catch (Exception e) {
            log.error("缓存执行(exist)失败：" + e.getMessage());
        }
        return false;
    }

    public static <K, F, V> boolean exist(HashCache<K, F, V> cache, K key, F field) {
        try {
            return cache.exist(key, field);
        } catch (Exception e) {
            log.error("缓存执行(exist)失败：" + e.getMessage());
        }
        return false;
    }

    public static <K,V> boolean exist(SetCache<K, V> cache, K key, V val) {
        try {
            if(exist(cache,key)){
                return cache.exist(key, val);
            }else{
                Set<V> vals = cache.onMiss(key);
                return vals.contains(val);
            }
        } catch (Exception e) {
            log.error("缓存执行(exist)失败：" + e.getMessage());
        }
        return false;
    }

}
