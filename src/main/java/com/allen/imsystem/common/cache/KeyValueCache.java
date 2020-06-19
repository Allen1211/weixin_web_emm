package com.allen.imsystem.common.cache;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName Cache
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/17
 * @Version 1.0
 */
public interface KeyValueCache<K,V> extends Cache<K>{

    /**
     * 从缓存获取key对应的value
     * @param key 缓存的key
     * @return 缓存的key对应的value
     */
    V get(K key);

    /**
     * 当缓存不命中时，调用该方法来完成数据的查找
     * 该方法的实现可以把查询到的数据加载到缓存，也可以不加载
     * @param key 缓存的key
     * @return 缓存的key对应的value
     */
    V onMiss(K key);

    /**
     * 当缓存执行失败时（抛出异常），调用该方法来完成数据的查找
     * 该方法的实现不应把数据加载到缓存(因为缓存执行已经抛出了异常)
     * @param key 缓存的key
     * @return 缓存的key对应的value
     */
    V onFail(K key);

    /**
     * 设置缓存的值
     * @param key 缓存的key
     * @param val 缓存的值
     */
    void set(K key, V val);

    /**
     * 移除缓存
     * @param key 缓存的key
     */
    void remove(K key);

    /**
     * 设置缓存的过期时间
     * @param key 缓存的key
     * @param timeUnit 时间单位
     * @param times 时间长度
     */
    void expired(K key, TimeUnit timeUnit, long times);

    /**
     * 判断是否缓存存在
     * @param key 缓存的key
     * @return true：存在
     */
    boolean exist(K key);

    /**
     * 递增缓存值，只对数字类型的缓存值有效
     * @param key 缓存的key
     * @param delta 递增的值
     * @return 递增后的缓存值
     */
    default V incr(K key, V delta){
        throw new UnsupportedOperationException("该缓存不支持递增");
    }
}
