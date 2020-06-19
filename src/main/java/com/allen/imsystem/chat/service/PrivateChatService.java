package com.allen.imsystem.chat.service;

import com.allen.imsystem.chat.model.dto.ChatCacheDTO;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;

import java.util.Date;
import java.util.Map;

/**
 * @ClassName PrivateChatService
 * @Description 私聊会话相关业务逻辑接口
 * @Author XianChuLun
 * @Date 2020/6/16
 * @Version 1.0
 */
public interface PrivateChatService {
    /**
     * 判断某个会话是否应该显示在会话列表（未被移除）
     *
     * @param uid    用户id
     * @param chatId 会话id
     * @return 是否应该显示在会话列表
     */
    boolean isOpen(String uid, Long chatId);

    /**
     * 获取私聊会话的一些信息
     *
     * @param chatId 会话id
     * @return 用ChatSessionInfo封装
     */
    ChatSessionInfo getChatSessionInfo(long chatId, String uid);

    /**
     * 初始化一个私聊会话
     *
     * @param uid      用户id
     * @param friendId 好友id
     * @param status   会话是否显示在会话列表
     * @return 私聊会话实体类
     */
    PrivateChat init(String uid, String friendId, Boolean status);

    /**
     * 开启一个对好友的私聊会话
     *
     * @param uid      用户id
     * @param friendId 好友id
     */
    Map<String, Object> open(String uid, String friendId);

    /**
     * 移除一个对好友的私聊会话
     *
     * @param uid    用户id
     * @param chatId 与好友的会话id
     */
    void remove(String uid, Long chatId);

    /**
     * 移除一个对好友的私聊会话
     *
     * @param uid      用户id
     * @param friendId 好友id
     */
    void remove(String uid, String friendId);

    /**
     * 标识一个私聊会话的所有消息已读
     *
     * @param uid    用户id
     * @param chatId 会话id
     */
    void setAllMsgHasRead(String uid, Long chatId);

    /**
     * 更新私聊会话的最后一条信息
     *
     * @param chatId            会话id
     * @param msgId             消息id
     * @param lastMsgContent    消息内容
     * @param lastMsgCreateTime 消息创建时间
     * @param senderId          发送者id
     */
    void updateLastMsg(Long chatId, Long msgId, String lastMsgContent,
                       Date lastMsgCreateTime, String senderId);

    ChatCacheDTO findChatCacheDTO(Long cahtId, String uid);
}
