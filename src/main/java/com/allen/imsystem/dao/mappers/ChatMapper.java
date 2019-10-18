package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.common.PageBean;
import com.allen.imsystem.model.dto.ChatNewMsgSizeDTO;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.pojo.ChatGroup;
import com.allen.imsystem.model.pojo.PrivateChat;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ChatMapper {
    List<ChatSessionDTO> selectPrivateChatList(String uid);

    @MapKey("talkId")
    Map<Long, ChatNewMsgSizeDTO> selectPrivateChatNewMsgSize(@Param("chatList") List<ChatSessionDTO> chatList,
                                                             @Param("uid")String uid);

    String selectPrivateChatTitleName(@Param("chatId")Long chatId, @Param("uid")String uid);

    ChatGroup selectChatGroupInfoByChatId(String chatId);

    PrivateChat selectPrivateChatInfoByUid(@Param("uidA")String uidA, @Param("uidB")String uidB);

    PrivateChat selectPrivateChatInfoByChatId(String chatId);

    List<MsgRecord> selectPrivateChatHistoryMsg(@Param("chatId")Long chatId,
                                                @Param("uid")String uid,@Param("pageBean") PageBean pageBean);

    Integer updatePrivateChat(PrivateChat privateChat);

    Integer setAllPrivateChatMsgHasRead(@Param("chatId")Long chatId, @Param("uid")String uid);
}
