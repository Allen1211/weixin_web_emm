package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.model.dto.ChatNewMsgSizeDTO;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ChatMapper {
    List<ChatSessionDTO> selectPrivateChatList(String uid);

    @MapKey("talkId")
    Map<Long, ChatNewMsgSizeDTO> selectPrivateChatNewMsgSize(@Param("chatList") List<ChatSessionDTO> chatList,
                                                             @Param("uid")String uid);
}
