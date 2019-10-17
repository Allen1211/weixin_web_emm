package com.allen.imsystem.dao;

import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.model.dto.ChatNewMsgSizeDTO;
import com.allen.imsystem.model.dto.ChatSessionDTO;
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
}
