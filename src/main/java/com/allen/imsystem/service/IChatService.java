package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.MsgRecord;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IChatService {


    String getChatType(String talkIdStr);

    String getChatType(Long talkId);




    /**
     * 开启一个对好友的私聊会话
     * @param uid
     * @param friendId
     */
    void openNewPrivateChat(String uid,String friendId);


    /**
     * 移除一个对好友的私聊会话
     * @param uid
     * @param chatId
     */
    void removePrivateChat(String uid,Long chatId);

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

    List<MsgRecord> getMessageRecord(String uid, String talkId,Integer index, Integer pageSize);

}
