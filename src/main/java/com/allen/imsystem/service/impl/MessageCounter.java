package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.mappers.ChatMapper;
import com.allen.imsystem.mappers.PrivateMsgRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 主要用于维护和获取会话消息未读数
 */
@Component
public class MessageCounter {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private PrivateMsgRecordMapper privateMsgRecordMapper;

    /**
     * 设置某用户会话的未读消息数，常用于初始化和清零。
     * @param uid
     * @param chatId
     * @param count
     * @return
     */
    public boolean setUserChatNewMsgCount(String uid, Long chatId, Integer count) {
        if (count == null) count = 0;
        return redisService.hset(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, uid + chatId, count);
    }

    /**
     * 获取私聊会话新消息条数
     * @param uid
     * @param chatId
     * @return
     */
    public Integer getPrivateChatNewMsgCount(String uid, Long chatId) {
        Integer count = (Integer) redisService.hget(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, uid + chatId);
        if (count == null) {  // 如果缓存中不存在，到数据库里查询，并更新缓存
            Integer newMsgCount = privateMsgRecordMapper.countPrivateChatUnReadMsgForUser(chatId, uid);
            setUserChatNewMsgCount(uid, chatId, newMsgCount);
            return newMsgCount;
        }
        return count;
    }

    /**
     * 获取群聊会话新消息条数
     * @param uid
     * @param gid
     * @return
     */
    public Integer getUserGroupChatNewMsgCount(String uid, String gid) {
        Integer count = (Integer) redisService.hget(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, uid + gid);
        return count == null ? 0 : count;
    }

    /**
     *
     * @param uid
     * @param chatId
     */
    public void incrPrivateChatNewMsgCount(String uid, Long chatId) {
        boolean hasKey = false;
        hasKey = redisService.hHasKey(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, uid + chatId);
        if (!hasKey) {
            // 去数据库里同步。
            Integer newMsgCount = privateMsgRecordMapper.countPrivateChatUnReadMsgForUser(chatId, uid);
            setUserChatNewMsgCount(uid, chatId, newMsgCount != null ? newMsgCount + 1 : 1);
            return;
        }
        Long val = redisService.hincr(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, uid + chatId, 1L);
        return;
    }

    /**
     *
     * @param memberIdSet
     * @param gid
     */
    public void incrGroupChatNewMsgCount(Set<Object> memberIdSet, String gid) {
        for (Object id : memberIdSet) {
            String key = id + gid;
            if (!redisService.hHasKey(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, key)) {
                redisService.hset(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, key, 1L);
            } else {
                redisService.hincr(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, key, 1L);
            }
        }
    }


}
