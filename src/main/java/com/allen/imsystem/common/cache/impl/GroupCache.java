package com.allen.imsystem.common.cache.impl;

import com.allen.imsystem.chat.mappers.GroupChatMapper;
import com.allen.imsystem.chat.mappers.GroupMapper;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.cache.SetCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName GroupCache
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/19
 * @Version 1.0
 */
@Lazy
@Service
public class GroupCache {

    @Autowired
    private GroupChatMapper groupChatMapper;
    @Autowired
    public MembersCache membersCache;

    @Component
    class MembersCache implements SetCache<String, String> {
        private final RedisTemplate<String, String> redisTemplate;
        private final SetOperations<String, String> setOperations;

        private final String BASE_KEY = GlobalConst.RedisKey.KET_GROUP_CHAT_MEMBERS;
        private final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.MINUTES;
        private final long DEFAULT_TIMES = 60L;

        @Autowired
        public MembersCache(RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
            this.setOperations = redisTemplate.opsForSet();
        }

        @Override
        public Set<String> get(String key) {
            return setOperations.members(BASE_KEY + key);
        }

        @Override
        public Set<String> onMiss(String key) {
            Set<String> groupMemberIdSet = groupChatMapper.selectGroupMemberIdSet(key);
            setOperations.add(BASE_KEY + key, groupMemberIdSet.toArray(new String[0]));
            redisTemplate.expire(BASE_KEY + key, DEFAULT_TIMES, DEFAULT_TIMEUNIT);
            return groupMemberIdSet;
        }

        @Override
        public Set<String> onFail(String key) {
            return groupChatMapper.selectGroupMemberIdSet(key);
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
            Boolean hasKey = redisTemplate.hasKey(BASE_KEY + key);
            return hasKey != null && hasKey;
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
