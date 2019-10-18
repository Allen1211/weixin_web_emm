package com.allen.imsystem.common.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class RedisUtil {
    /**
     * 日志记录
     */
    private static final Logger LOG = LoggerFactory.getLogger(RedisUtil.class);
    /**
     * redis 连接池
     */
    @Autowired
    private JedisPool jedisPool;

    private static final int EXPRIED_TIME = 60 * 60 * 3;    // 三小时

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Spring Data Redis
     */
    /**
     * 获取jedis
     *
     * @return
     */
    public Jedis getResource() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.auth("123456");
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("can't  get  the redis resource");
            if (jedis != null)
                jedis.close();
        }
        return jedis;
    }

    /**
     * 关闭连接
     *
     * @param jedis
     */
    public void disconnect(Jedis jedis) {
        jedis.disconnect();
    }

    /**
     * 将jedis 返还连接池
     *
     * @param jedis
     */
    public void close(Jedis jedis) {
        if (null != jedis) {
            jedis.close();
        }
    }

    private String valueToString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            // TODO 对象序列化
            return null;
        }
    }

//    private  <T> T castStringToT(String value, Class<T> clazz){
//        String clazzName = clazz.getName();
//        if(clazzName.equals(String.class.getName())){
//            return (T) value;
//        }else if(clazz.getSuperclass().getName().equals(Number.class.getName())){
//            return value.toString();
//        }
//    }


    /**
     * 在redis数据库中插入 key  和value
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, Object value) {
        return set(key, value, EXPRIED_TIME);
    }

    /**
     * 在redis数据库中插入 key  和value 并且设置过期时间
     *
     * @param key
     * @param value
     * @param exp   过期时间,秒
     * @return
     */
    public boolean set(String key, Object value, int exp) {
        String strVal = valueToString(value);
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(strVal)) {
            Jedis jedis = jedisPool.getResource();
            jedis.setex(key, exp, strVal);
            jedis.close();
            return true;
        }
        return false;
    }

    /**
     * 根据key 去redis 中获取value
     *
     * @param key
     * @return
     */
    public String get(String key) {
        if (StringUtils.isNotEmpty(key)) {
            Jedis jedis = jedisPool.getResource();
            String value = jedis.get(key);
            jedis.close();
            return value;
        } else {
            LOG.info("redis get key is empty");
            return null;
        }
    }

    /**
     * 删除redis库中的数据
     *
     * @param key
     * @return
     */
    public boolean remove(String key) {
        if (StringUtils.isNotEmpty(key)) {
            Jedis jedis = jedisPool.getResource();
            jedis.del(key);
            jedis.close();
            return true;
        } else {
            LOG.info("redis remove key is empty");
            return false;
        }
    }

    /**
     * 设置哈希类型数据到redis 数据库
     *
     * @param key   表字段
     * @param value
     * @return
     */
    public boolean hset(String key, String field, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field) || StringUtils.isEmpty(value)) {
            LOG.info("hset params empty");
            return false;
        }
        Jedis jedis = jedisPool.getResource();
        jedis.hset(key, field, value);
        jedis.close();
        return true;
    }

    public boolean hset(String key, Map<String, String> map) {
        if (StringUtils.isEmpty(key)) {
            LOG.info("hset params empty");
            return false;
        }
        Jedis jedis = jedisPool.getResource();
        jedis.hset(key, map);
        jedis.close();
        return true;
    }

    /**
     * 获取哈希表数据类型的值
     *
     * @param key
     * @return
     */
    public String hget(String key, String field) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(field)) {
            LOG.info("hget params empty");
            return null;
        }
        Jedis jedis = jedisPool.getResource();
        String value = jedis.hget(key, field);
        jedis.close();
        return value;
    }

    /**
     * 获取哈希类型的数据
     *
     * @return
     */
    Map<String, String> hget(String key) {
        if (StringUtils.isEmpty(key)) {
            LOG.info("hget params empty");
            return null;
        }
        Jedis jedis = jedisPool.getResource();
        Map<String, String> map = jedis.hgetAll(key);
        jedis.close();
        return map;
    }

}