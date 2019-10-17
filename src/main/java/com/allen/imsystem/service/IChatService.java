package com.allen.imsystem.service;

import com.allen.imsystem.model.dto.ChatSessionDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IChatService {

    /**
     * 开启一个对好友的私聊会话
     * @param uid
     * @param friendId
     */
    void openNewPrivateChat(String uid,String friendId);

    /**
     * 关闭一个对好友的私聊会话
     * @param uid
     * @param friendId
     * @param chatId
     */
    void closePrivateChat(String uid,String friendId, Long chatId);

    /**
     * 获取会话列表
     * @param uid
     */
    List<ChatSessionDTO> getChatList(String uid);


}
