package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.PrivateChat;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface IChatService {

    /**
     * 获得会话类型
     * @param talkIdStr
     * @return
     */
    Integer getChatType(String talkIdStr);
    Integer getChatType(Long talkId);

    /**
     * 判断某个会话是否被用户移除
     * @param uid
     * @param chatId
     * @return
     */
    Boolean isChatSessionOpenToUser(String uid, Long chatId);
    Boolean isChatSessionOpenToUser(String uid, String chatId);

    /**
     * 获取某个会话最后一条消息的时间
     * @param chatId
     * @return
     */
    Long getChatLastMsgTimestamp(Long chatId);

    /**
     * 设置某个会话最后一条消息的时间
     * @param chatId
     * @param timestamp
     * @return
     */
    boolean setChatLastMsgTimestamp(Long chatId, Long timestamp);

    /**
     * 设置某用户会话的未读消息数，常用于初始化和清零。
     * @param uid
     * @param chatId
     * @param count
     * @return
     */
    boolean setUserChatNewMsgCount(String uid,Long chatId, Integer count);

    Integer getUserChatNewMsgCount(String uid,Long chatId);

    void incrUserChatNewMsgCount(String uid, Long chatId);

    /**
     * 初始化一个私聊会话
     * @param uid
     * @param friendId
     * @param status
     * @return
     */
    PrivateChat initNewPrivateChat(String uid,String friendId,Boolean status);

    /**
     * 开启一个对好友的私聊会话
     * @param uid
     * @param friendId
     */
    Map<String, Object> openNewPrivateChat(String uid, String friendId);


    /**
     * 移除一个对好友的私聊会话
     * @param uid
     * @param chatId
     */
    void removePrivateChat(String uid,Long chatId);

    /**
     * 移除一个对好友的私聊会话
     * @param uid
     * @param friendId
     */
    void removePrivateChat(String uid,String friendId);

    /**
     * 获取会话列表
     * @param uid
     */
    List<ChatSessionDTO> getChatList(String uid);

    /**
     * 获取会话的一些信息
     * @param talkId
     * @return
     */
    ChatSessionInfo getChatInfo(Long talkId,String uid);

    /**
     * 标识已读
     * @param uid
     * @param talkId
     * @return
     */
    Boolean setTalkAllMsgHasRead(String uid, String talkId);

    /**
     * 获取一个会话的聊天记录
     * @param uid
     * @param talkId
     * @param index
     * @param pageSize
     * @return
     */
    List<MsgRecord> getMessageRecord(String uid, String talkId,Date beginTime,Integer index, Integer pageSize);

    /**
     * 获取某个会话所有聊天记录的条数
     * @param talkId
     * @param uid
     * @param beginTime
     * @return
     */
    Integer getAllHistoryMessageSize(String talkId, String uid, Date beginTime);

    /**
     * 私聊消息入库
     * @param sendMsgDTO
     * @return
     */
    Boolean savePrivateMsgRecord(SendMsgDTO sendMsgDTO);

    /**
     * 更新会话的最后一条信息
     * @param chatId
     * @param msgId
     * @param senderId
     * @return
     */
    Boolean updateChatLastMsg(Long chatId, Long msgId,String senderId);
}
