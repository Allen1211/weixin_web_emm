package com.allen.imsystem.common.cache.impl;

import com.allen.imsystem.chat.model.dto.ChatCacheDTO;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.cache.HashCache;
import com.allen.imsystem.common.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ChatCache
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/18
 * @Version 1.0
 */
@Service
public class ChatCache {

    @Autowired
    private ChatService chatService;

    @Autowired
    private RedisService redisService;

    public final ChatInfoCache chatInfoCache = new ChatInfoCache();

    public static final String SHOULD_DISPLAY = "should_display";
    public static final String UNREAD_MSG_COUNT = "unread_msg_count";
    public static final String LAST_MSG_TIME = "last_msg_time";
    public static final String RECORD_BEGIN_ID = "last_record_begin_id";
    public static final String GID = "gid";

    public static String wrapChatInfoKey(Long chatId, String uid) {
        return chatId + "#" + uid;
    }

    class ChatInfoCache implements HashCache<String, String, Object> {

        public static final String BASE_KEY = GlobalConst.RedisKey.KEY_CHAT_INFO;

        private static final long DEFAULT_EXPIRED_TIME = 15L;
        private final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

        private final Set<String> fieldSet;

        public ChatInfoCache() {
            fieldSet = new HashSet<>(5);
            fieldSet.add(SHOULD_DISPLAY);
            fieldSet.add(UNREAD_MSG_COUNT);
            fieldSet.add(LAST_MSG_TIME);
            fieldSet.add(RECORD_BEGIN_ID);
            fieldSet.add(GID);
        }

        @Override
        public Object get(String key, String field) {
            assertFieldExist(field);
            return redisService.hget(GlobalConst.RedisKey.KEY_CHAT_INFO + key, field);
        }

        @Override
        public Object onMiss(String key, String field) {
            return loadFieldData(key, field);
        }

        @Override
        public Object onFail(String key, String field) {
            if (field.equals(RECORD_BEGIN_ID)) {
                return null;
            } else {
                String[] parts = key.split("#");
                Long chatId = Long.parseLong(parts[0]);
                String uid = parts[1];
                ChatCacheDTO chatCacheDTO = chatService.findChatCacheDTO(chatId, uid);
                if (chatCacheDTO != null) {
                    switch (field) {
                        case LAST_MSG_TIME:
                            return chatCacheDTO.getLastMsgTimestamp();
                        case UNREAD_MSG_COUNT:
                            return chatCacheDTO.getUnreadMsgCount();
                        case SHOULD_DISPLAY:
                            return chatCacheDTO.getShouldDisplay();
                        case GID:
                            return chatCacheDTO.getGid();
                    }
                }
            }
            return null;
        }

        @Override
        public void set(String key, String field, Object val) {
            redisService.hset(BASE_KEY + key, field, val, DEFAULT_EXPIRED_TIME, DEFAULT_TIME_UNIT);
        }

        @Override
        public void remove(String key, String field) {
            redisService.hdel(BASE_KEY + key, field);
        }

        @Override
        public boolean exist(String key) {
            return redisService.hasKey(BASE_KEY + key);
        }

        @Override
        public void remove(String key) {
            redisService.del(BASE_KEY + key);
        }

        @Override
        public void expired(String key, TimeUnit timeUnit, long times) {
            redisService.expire(BASE_KEY + key, times, timeUnit);
        }

        @Override
        public boolean exist(String key, String field) {
            return redisService.hHasKey(BASE_KEY + key, field);
        }

        @Override
        public Object incr(String key, String field, Object delta) {
            if (!field.equals(UNREAD_MSG_COUNT)) {
                throw new UnsupportedOperationException("该 field 不支持递增: " + field);
            }
            if (!exist(key, field)) {
                onMiss(key, field);
            }
            return redisService.hincr(BASE_KEY + key, field, (Long) delta);
        }

        private Object loadFieldData(String key, String field) {
            if (field.equals(RECORD_BEGIN_ID)) {
                return null;
            } else {
                String[] parts = key.split("#");
                Long chatId = Long.parseLong(parts[0]);
                String uid = parts[1];
                ChatCacheDTO chatCacheDTO = chatService.findChatCacheDTO(chatId, uid);
                if (chatCacheDTO != null) {
                    set(key, LAST_MSG_TIME, chatCacheDTO.getLastMsgTimestamp());
                    set(key, UNREAD_MSG_COUNT, chatCacheDTO.getUnreadMsgCount());
                    set(key, SHOULD_DISPLAY, chatCacheDTO.getShouldDisplay());
                    if (chatCacheDTO.isGroup()) {
                        set(key, GID, chatCacheDTO.getGid());
                    }
                    switch (field) {
                        case LAST_MSG_TIME:
                            return chatCacheDTO.getLastMsgTimestamp();
                        case UNREAD_MSG_COUNT:
                            return chatCacheDTO.getUnreadMsgCount();
                        case SHOULD_DISPLAY:
                            return chatCacheDTO.getShouldDisplay();
                        case GID:
                            return chatCacheDTO.getGid();
                    }
                }
            }
            return null;
        }

        private void assertFieldExist(String field) throws IllegalArgumentException {
            if (!fieldSet.contains(field)) {
                throw new IllegalArgumentException("不支持的field : " + field);
            }
        }


    }
}
