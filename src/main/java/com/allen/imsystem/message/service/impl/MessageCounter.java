package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.chat.mappers.GroupChatMapper;
import com.allen.imsystem.chat.mappers.PrivateChatMapper;
import com.allen.imsystem.chat.model.dto.ChatIdUidDTO;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.cache.CacheExecutor;
import com.allen.imsystem.common.cache.impl.ChatCache;
import com.allen.imsystem.message.mappers.PrivateMsgRecordMapper;
import com.allen.imsystem.common.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * 主要用于维护和获取会话消息未读数
 */
@Component
public class MessageCounter {

    @Autowired
    private ChatService chatService;

    @Autowired
    private PrivateChatMapper privateChatMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private ChatCache chatCache;

    /**
     * 设置某用户会话的未读消息数，常用于初始化和清零。
     *
     * @param uid
     * @param chatId
     * @param count
     * @return
     */
    public void setUserChatNewMsgCount(String uid, Long chatId, Integer count) {
        if (count == null) count = 0;
        if (chatService.getChatType(chatId) == GlobalConst.ChatType.PRIVATE_CHAT) {
            PrivateChat privateChat = privateChatMapper.findByChatId(chatId);
            if(privateChat.getUidA().equals(uid)){
                privateChat.setUserAUnreadMsgCount(0);
            }else{
                privateChat.setUserBUnreadMsgCount(0);
            }
            privateChatMapper.update(privateChat);
        } else {
            GroupChat groupChat = new GroupChat();
            groupChat.setChatId(chatId);
            groupChat.setUnreadMsgCount(0);
            groupChatMapper.update(groupChat);
        }
        CacheExecutor.set(chatCache.chatInfoCache, chatId + "#" + uid,
                ChatCache.UNREAD_MSG_COUNT, count);
    }

    /**
     * 获取私聊会话新消息条数
     *
     * @param uid
     * @param chatId
     * @return
     */
    public Integer getPrivateChatNewMsgCount(String uid, Long chatId) {
        return (Integer) CacheExecutor.get(chatCache.chatInfoCache, chatId + "#" + uid, ChatCache.UNREAD_MSG_COUNT);
    }

    /**
     * 获取群聊会话新消息条数
     *
     * @param uid    用户uid
     * @param chatId 会话id
     */
    public Integer getUserGroupChatNewMsgCount(String uid, Long chatId) {
        return (Integer) CacheExecutor.get(chatCache.chatInfoCache, chatId + "#" + uid, ChatCache.UNREAD_MSG_COUNT);

    }

    /**
     * 递增私聊会话的未读新消息条数
     *
     * @param uid    用户uid
     * @param chatId 私聊会话id
     */
    public void incrPrivateChatNewMsgCount(String uid, Long chatId) {
        privateChatMapper.incrUnreadMsgCount(uid, chatId);
        CacheExecutor.incr(chatCache.chatInfoCache, chatId + "#" + uid, ChatCache.UNREAD_MSG_COUNT, 1L);
    }

    /**
     * 递增群会话的未读新消息条数
     *
     * @param gid      群id
     * @param senderId 发送者id
     */
    @Transactional
    public void incrGroupChatNewMsgCount(String senderId, String gid) {
        // 查询这些成员的群会话id
        List<ChatIdUidDTO> chatIdUidList = groupChatMapper.findChatIdListByGidExcludeUid(senderId, gid);
        if (!CollectionUtils.isEmpty(chatIdUidList)) {
            // 递增数据库里的未读消息数
            int affects = groupChatMapper.incrUnreadMsgBatchByChatIdList(chatIdUidList);
            if (affects > 0) {
                // 递增对应的缓存
                for (ChatIdUidDTO chatIdUidDTO : chatIdUidList) {
                    CacheExecutor.incr(chatCache.chatInfoCache, chatIdUidDTO.getChatId() + "#" + chatIdUidDTO.getUid(),
                            ChatCache.UNREAD_MSG_COUNT, 1L);
                }
            }
        }
    }


}
