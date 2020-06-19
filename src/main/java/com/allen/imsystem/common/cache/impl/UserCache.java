package com.allen.imsystem.common.cache.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.cache.KeyValueCache;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.user.model.vo.UserInfoView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.allen.imsystem.common.Const.GlobalConst.RedisKey;

/**
 * @ClassName UserCache
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/17
 * @Version 1.0
 */
@Service
public class UserCache {

    public final UserInfoViewKeyValueCache userInfoViewCache = new UserInfoViewKeyValueCache();

    public final UserOnlineStatusKeyValueCache userOnlineStatusCache = new UserOnlineStatusKeyValueCache();

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    class UserInfoViewKeyValueCache implements KeyValueCache<String, UserInfoView> {

        @Override
        public UserInfoView get(String key) {
            if (key == null) {
                return null;
            }
            return (UserInfoView) redisService.get(RedisKey.KEY_USER_INFO + key);
        }

        @Override
        public UserInfoView onMiss(String key) {
            UserInfoView val = userMapper.selectUserInfoDTO(key);
            redisService.set(RedisKey.KEY_USER_INFO + key, val, 15L, TimeUnit.MINUTES);
            return val;
        }

        @Override
        public UserInfoView onFail(String key) {
            return userMapper.selectUserInfoDTO(key);
        }

        @Override
        public void set(String key, UserInfoView val) {
            redisService.set(RedisKey.KEY_USER_INFO + key, val, 15L, TimeUnit.MINUTES);
        }

        @Override
        public void remove(String key) {
            redisService.del(RedisKey.KEY_USER_INFO + key);
        }

        @Override
        public void expired(String key, TimeUnit timeUnit, long times) {
            redisService.expire(RedisKey.KEY_USER_INFO + key, times, timeUnit);
        }

        @Override
        public boolean exist(String key) {
            return redisService.hasKey(key);
        }
    }

    class UserOnlineStatusKeyValueCache implements KeyValueCache<String, Integer> {

        @Override
        public Integer get(String key) {
            Integer onlineStatus = (Integer) redisService.hget(RedisKey.KEY_USER_STATUS, key);
            if (onlineStatus == null) {
                onlineStatus = GlobalConst.UserStatus.ONLINE;
            }
            return onlineStatus;
        }

        @Override
        public Integer onMiss(String key) {
            return GlobalConst.UserStatus.ONLINE;
        }

        @Override
        public Integer onFail(String key) {
            return GlobalConst.UserStatus.ONLINE;
        }

        @Override
        public void set(String key, Integer val) {
            redisService.hset(RedisKey.KEY_USER_STATUS, key, val);
        }

        @Override
        public void remove(String key) {
            redisService.hdel(RedisKey.KEY_USER_STATUS, key);
        }

        @Override
        public void expired(String key, TimeUnit timeUnit, long times) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean exist(String key) {
            return redisService.hasKey(key);
        }

    }

}
