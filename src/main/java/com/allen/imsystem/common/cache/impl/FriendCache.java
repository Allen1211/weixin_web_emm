package com.allen.imsystem.common.cache.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.cache.SetCache;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.friend.mappers.FriendMapper;
import com.allen.imsystem.friend.service.FriendQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName FriendCache
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/19
 * @Version 1.0
 */
@Service
public class FriendCache {

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    public FriendSetCache friendSet;

    @Component
    class FriendSetCache implements SetCache<String, String> {

        private final RedisTemplate<String, String> redisTemplate;
        private final SetOperations<String, String> setOperations;

        private final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.MINUTES;
        private final long DEFAULT_TIMES = 60L;

        @Autowired
        public FriendSetCache(RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
            this.setOperations = redisTemplate.opsForSet();
        }

        public static final String BASE_KEY = GlobalConst.RedisKey.KEY_FRIEND_SET;

        @Override
        public Set<String> get(String key) {
            return setOperations.members(BASE_KEY + key);
        }

        @Override
        public Set<String> onMiss(String key) {
            Set<String> friendIdSet = friendMapper.selectTwoWayFriendId(key);
            if (friendIdSet != null) {
                setOperations.add(BASE_KEY + key, friendIdSet.toArray(new String[0]));
                expired(key, DEFAULT_TIMEUNIT, DEFAULT_TIMES);
                return friendIdSet;
            } else {
                return Collections.emptySet();
            }
        }

        @Override
        public Set<String> onFail(String key) {
            Set<String> friendIdSet = friendMapper.selectTwoWayFriendId(key);
            if (friendIdSet != null) {
                return friendIdSet;
            } else {
                return Collections.emptySet();
            }
        }

        @Override
        public void set(String key, Set<String> vals) {
            setOperations.add(BASE_KEY + key, vals.toArray(new String[0]));
        }

        @Override
        public void add(String key, String val) {
            setOperations.add(BASE_KEY + key, val);
        }

        @Override
        public boolean exist(String key) {
            Boolean exist = redisTemplate.hasKey(BASE_KEY + key);
            return exist != null && exist;
        }

        @Override
        public void remove(String key) {
            redisTemplate.delete(BASE_KEY + key);
        }

        @Override
        public void remove(String key, String val) {
            setOperations.remove(BASE_KEY + key, val);
        }

        @Override
        public void expired(String key, TimeUnit timeUnit, long times) {
            redisTemplate.expire(BASE_KEY + key, times, timeUnit);
        }

        @Override
        public boolean exist(String key, String val) {
            Boolean exist = setOperations.isMember(BASE_KEY + key, val);
            return exist != null && exist;
        }
    }
}
