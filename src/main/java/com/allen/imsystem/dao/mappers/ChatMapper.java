package com.allen.imsystem.dao.mappers;

import com.allen.imsystem.common.PageBean;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.ChatGroup;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.model.pojo.PrivateMsgRecord;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ChatMapper {
    List<ChatSessionDTO> selectPrivateChatList(String uid);

    ChatSessionDTO selectNewMsgPrivateChatData(@Param("chatId") Long chatId,
                                               @Param("uid")String uid ,@Param("friendId")String friendId);

    @MapKey("talkId")
    Map<Long, ChatNewMsgSizeDTO> selectPrivateChatNewMsgSize(@Param("chatList") List<ChatSessionDTO> chatList,
                                                             @Param("uid")String uid);

    String selectPrivateChatTitleName(@Param("chatId")Long chatId, @Param("uid")String uid);

    ChatGroup selectChatGroupInfoByChatId(String chatId);

    PrivateChat selectPrivateChatInfoByUid(@Param("uidA")String uidA, @Param("uidB")String uidB);

    PrivateChat selectPrivateChatInfoByChatId(String chatId);

    ChatSessionInfo selectPrivateChatData(@Param("chatId")Long chatId, @Param("uid")String uid);


    List<MsgRecord> selectPrivateChatHistoryMsg(@Param("chatId")Long chatId,@Param("beginTime")Date beginTime,
                                                @Param("uid")String uid,@Param("pageBean") PageBean pageBean);

    Integer countAllHistoryMsg(@Param("chatId")Long chatId, @Param("beginTime")Date beginTime);

    Integer countPrivateChatUnReadMsgForUser(@Param("chatId")Long chatId, @Param("uid")String uid);

    Integer insertNewPrivateChat(PrivateChat privateChat);

    Integer updatePrivateChat(PrivateChat privateChat);

    Integer updatePrivateChatLastMsg(@Param("chatId")Long chatId, @Param("msgId")Long msgId,
                                     @Param("lastSenderId")String lastSenderId);

    Integer setAllPrivateChatMsgHasRead(@Param("chatId")Long chatId, @Param("uid")String uid);

    Integer hardDeletePrivateChat(@Param("uidA")String uidA,@Param("uidB")String uidB);

    Integer insertPrivateMsgToRecord(PrivateMsgRecord privateMsgRecord);


    List<ChatLastMessageTime> selectChatSessionLastMsgTime();
}
