package com.allen.imsystem.common.cache;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName Cache
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/19
 * @Version 1.0
 */
public interface Cache<K> {

    /**
     * 判断是否缓存存在
     * @param key 缓存的key
     * @return true：存在
     */
    boolean exist(K key);


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


}
