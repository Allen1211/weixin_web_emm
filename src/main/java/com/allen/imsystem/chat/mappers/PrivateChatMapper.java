package com.allen.imsystem.chat.mappers;

import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @ClassName PrivateChatMapper
 * @Description 私聊会话数据库表操作
 * @Author XianChuLun
 * @Date 2020/5/26
 * @Version 1.0
 */
@Mapper
@Repository
public interface PrivateChatMapper {
    /**
     * 插入私聊会话表
     * @param privateChat 私聊会话pojo
     */
    int insert(PrivateChat privateChat);

    /**
     * 更新私聊会话表
     * @param privateChat 私聊会话pojo
     */
    int update(PrivateChat privateChat);

    /**
     * 更新私聊会话的最后一条消息
     * @param chatId 会话id
     * @param msgId 消息id
     * @param lastSenderId 最后一条消息的发送者id
     */
    int updatePrivateChatLastMsg(@Param("chatId") Long chatId, @Param("msgId") Long msgId,
                                 @Param("lastMsgContent") String lastMsgContent,
                                 @Param("lastMsgCreateTime") Date lastMsgCreateTime,
                                 @Param("lastSenderId") String lastSenderId);

    /**
     * 根据uidA，uidB查询私聊会话实体类
     */
    PrivateChat findByUidAB(@Param("uidA") String uidA, @Param("uidB") String uidB);

    /**
     * 根据chatId查询私聊会话实体类
     */
    PrivateChat findByChatId(Long chatId);

    /**
     * 根据uid查询该用户的私聊会话列表
     * @param uid 用户uid
     */
    List<ChatSession> findChatSessionListByUid(String uid);

    /**
     *
     * @param chatId
     * @param uid
     * @param friendId
     * @return
     */
    ChatSession selectNewMsgPrivateChatData(@Param("chatId") Long chatId,
                                            @Param("uid") String uid, @Param("friendId") String friendId);

    ChatSessionInfo selectPrivateChatData(@Param("chatId") Long chatId, @Param("uid") String uid);


}
