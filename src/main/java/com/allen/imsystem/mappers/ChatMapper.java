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

    List<ChatSession> selectGroupChatList(String uid);

    ChatSessionInfo selectGroupChatData(Long chatId);

    List<MsgRecord> selectGroupChatHistoryMsg(@Param("gid") String gid, @Param("beginMsgId") Long beginMsgId,
                                              @Param("uid") String uid, @Param("pageBean") PageBean pageBean);

    Integer countAllGroupHistoryMsg(@Param("gid") String gid, @Param("beginTime") Date beginTime);

}
