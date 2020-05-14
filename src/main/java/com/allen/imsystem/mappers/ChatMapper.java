package com.allen.imsystem.mappers;

import com.allen.imsystem.common.PageBean;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.model.pojo.PrivateMsgRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
@Mapper
public interface ChatMapper {
    List<ChatSessionDTO> selectPrivateChatList(String uid);

    List<ChatSessionDTO> selectGroupChatList(String uid);

    ChatSessionDTO selectNewMsgPrivateChatData(@Param("chatId") Long chatId,
                                               @Param("uid") String uid, @Param("friendId") String friendId);

    PrivateChat selectPrivateChatInfoByUid(@Param("uidA") String uidA, @Param("uidB") String uidB);

    PrivateChat selectPrivateChatInfoByChatId(Long chatId);

    ChatSessionInfo selectPrivateChatData(@Param("chatId") Long chatId, @Param("uid") String uid);

    ChatSessionInfo selectGroupChatData(Long chatId);

    List<MsgRecord> selectPrivateChatHistoryMsg(@Param("chatId") Long chatId, @Param("beginMsgId") Long beginMsgId,
                                                @Param("uid") String uid, @Param("pageBean") PageBean pageBean);

    List<MsgRecord> selectGroupChatHistoryMsg(@Param("gid") String gid, @Param("beginMsgId") Long beginMsgId,
                                              @Param("uid") String uid, @Param("pageBean") PageBean pageBean);

    Integer countAllPrivateHistoryMsg(@Param("chatId") Long chatId, @Param("beginTime") Date beginTime);

    Integer countAllGroupHistoryMsg(@Param("gid") String gid, @Param("beginTime") Date beginTime);

    Integer countPrivateChatUnReadMsgForUser(@Param("chatId") Long chatId, @Param("uid") String uid);

    Integer insertNewPrivateChat(PrivateChat privateChat);

    Integer updatePrivateChat(PrivateChat privateChat);

    Integer updatePrivateChatLastMsg(@Param("chatId") Long chatId, @Param("msgId") Long msgId,
                                     @Param("lastSenderId") String lastSenderId);

    Integer setAllPrivateChatMsgHasRead(@Param("chatId") Long chatId, @Param("uid") String uid);

    Integer hardDeletePrivateChat(@Param("uidA") String uidA, @Param("uidB") String uidB);

    Integer insertPrivateMsgToRecord(PrivateMsgRecord privateMsgRecord);

    List<PrivateChat> fix1();
}
