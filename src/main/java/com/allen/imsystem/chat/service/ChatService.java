package com.allen.imsystem.chat.service;

import com.allen.imsystem.chat.model.dto.ChatCacheDTO;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface ChatService {

    /**
     * 获得会话类型
     * @return
     */
    int getChatType(Long chatId);

    /**
     * 获取会话列表
     * @param uid
     */
    List<ChatSession> getChatList(String uid);

    /**
     * 获取会话的一些信息
     * @param chatId
     * @return
     */
    Map<String,Object> getChatInfo(Long chatId, String uid);

    /**
     * 标识一个会话的所有消息已读
     * @param chatType 会话类型
     * @param uid 用户id
     * @param id 会话或是群的id，若为私聊会话，则为chatId, 若为群聊，则为gid
     */
    void setChatAllMsgHasRead(int chatType, String uid, String id);

    /**
     * 更新会话的最后一条信息
     * @param chatType 会话类型
     * @param id 会话或是群的id，若为私聊会话，则为chatId, 若为群聊，则为gid
     * @param msgId 消息id
     * @param lastMsgContent 消息内容
     * @param lastMsgCreateTime 消息创建时间
     * @param senderId 发送者id
     */
    void updateChatLastMsg(int chatType ,String id, Long msgId, String lastMsgContent,
                           Date lastMsgCreateTime, String senderId);

    Map<String,Object> validateChatId(Long chatId, String uid);

    /**
     * 获取会话的信息用于缓存
     * @param chatId 会话id
     * @param uid 用户uid
     * @return 用于缓存的会话信息
     */
    ChatCacheDTO findChatCacheDTO(Long chatId, String uid);


}
