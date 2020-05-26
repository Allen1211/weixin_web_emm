package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.ChatSession;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.model.pojo.PrivateMsgRecord;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface IChatService {

    boolean isGroupChat(Long chatId);

    /**
     * 获得会话类型
     * @return
     */
    Integer getChatType(String chatIdStr);
    Integer getChatType(Long chatId);

    /**
     * 判断某个会话是否被用户移除
     * @param uid
     * @param chatId
     * @return
     */
    Boolean isPrivateChatSessionOpenToUser(String uid, Long chatId);
    Boolean isGroupChatSessionOpenToUser(String uid, String gid);

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
     * 初始化一个私聊会话
     * @param uid
     * @param friendId
     * @param status
     * @return
     */
    PrivateChat initNewPrivateChat(String uid, String friendId, Boolean status);

    /**
     * 开启一个对好友的私聊会话
     * @param uid
     * @param friendId
     */
    Map<String, Object> openNewPrivateChat(String uid, String friendId);

    /**
     * 开启一个群聊会话
     * @param uid
     * @param gid
     * @return
     */
    Map<String, Object> openGroupChat(String uid, String gid);

    /**
     * 移除一个对好友的私聊会话
     * @param uid
     * @param chatId
     */
    void removePrivateChat(String uid, Long chatId);

    /**
     * 移除一个对好友的私聊会话
     * @param uid
     * @param friendId
     */
    void removePrivateChat(String uid, String friendId);

    /**
     * 移除一个群聊会话
     * @param uid
     * @param chatId
     */
    void removeGroupChat(String uid, Long chatId);

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
    ChatSessionInfo getChatInfo(Long chatId, String uid);

    /**
     * 标识一个私聊会话的所有消息已读
     * @param uid
     * @param chatId
     * @return
     */
    Boolean setPrivateChatAllMsgHasRead(String uid, Long chatId);
    /**
     * 标识一个群聊会话的所有消息已读
     * @param uid
     * @return
     */
    Boolean setGroupChatAllMsgHasRead(String uid, String gid);

    /**
     * 获取一个会话的聊天记录
     * @param uid
     * @param chatId
     * @param index
     * @param pageSize
     * @return
     */
    Map<String,Object> getMessageRecord(boolean isGroup, String uid, Long chatId, Integer index, Integer pageSize);

    /**
     * 获取某个会话所有聊天记录的条数
     * @param chatId
     * @param uid
     * @param beginTime
     * @return
     */
    Integer getAllHistoryMessageSize(boolean isgGroup, Long chatId, String uid, Date beginTime);


    /**
     * 私聊消息入库
     * @param sendMsgDTO
     * @return
     */
    PrivateMsgRecord savePrivateMsgRecord(SendMsgDTO sendMsgDTO);

    /**
     * 更新会话的最后一条信息
     * @param chatId 会话id
     * @param msgId 消息id
     * @param senderId 发送者id
     * @param lastMsgContent 消息内容
     * @param lastMsgCreateTime 消息创建时间
     * @return
     */
    boolean updateChatLastMsg(Long chatId, Long msgId, String lastMsgContent,
                              Date lastMsgCreateTime, String senderId);

    Map<String,Object> validateChatId(Long chatId, String uid);

}
