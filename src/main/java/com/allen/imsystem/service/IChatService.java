package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.pojo.PrivateChat;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public interface IChatService {


    String getChatType(String talkIdStr);

    String getChatType(Long talkId);

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
    ChatSessionInfo getChatInfo(String talkId,String uid);

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


    Integer getAllHistoryMessageSize(String talkId, String uid, Date beginTime);
}
