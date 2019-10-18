package com.allen.imsystem.dao;

import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.model.dto.ChatNewMsgSizeDTO;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.pojo.ChatGroup;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ChatDao {


    @Autowired
    private ChatMapper chatMapper;

    public List<ChatSessionDTO> selectPrivateChatList(String uid){
        return chatMapper.selectPrivateChatList(uid);
    }

    public Map<Long, ChatNewMsgSizeDTO> selectPrivateChatNewMsgSize(List<ChatSessionDTO> chatList, String uid){
        return chatMapper.selectPrivateChatNewMsgSize(chatList,uid);
    }

    public String selectPrivateChatTitleName(@Param("chatId")Long chatId, @Param("uid")String uid){
        return chatMapper.selectPrivateChatTitleName(chatId,uid);
    }

    public ChatGroup selectChatGroupInfoByChatId(String chatId){
        return chatMapper.selectChatGroupInfoByChatId(chatId);
    }
}
