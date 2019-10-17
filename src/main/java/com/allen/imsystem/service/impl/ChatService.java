package com.allen.imsystem.service.impl;

import com.allen.imsystem.dao.ChatDao;
import com.allen.imsystem.model.dto.ChatNewMsgSizeDTO;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ChatService implements IChatService {

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private IUserService userService;

    @Override
    public void openNewPrivateChat(String uid, String friendId) {

    }

    @Override
    public void closePrivateChat(String uid, String friendId, Long chatId) {

    }

    @Override
    public List<ChatSessionDTO> getChatList(String uid) {
        // 获取私聊会话
        List<ChatSessionDTO> privateChatList = chatDao.selectPrivateChatList(uid);
        Map<Long, ChatNewMsgSizeDTO> sizeMap = chatDao.selectPrivateChatNewMsgSize(privateChatList,uid);
        if(privateChatList != null){
            for(int i=0;i<privateChatList.size();i++){
                ChatSessionDTO privateChat = privateChatList.get(i);
                // 填充新消息条数
                ChatNewMsgSizeDTO sizeDTO = sizeMap.get(privateChat.getTalkId());
                privateChat.setNewMessageCount(sizeDTO == null ? 0:sizeDTO.getSize());

                // 填充在线信息
                String onlineStatus = userService.getUserOnlineStatus(privateChat.getFriendId());
                privateChat.setOnline(onlineStatus.equals("1"));
            }
        }
        // 获取群会话

        return privateChatList;
    }
}
