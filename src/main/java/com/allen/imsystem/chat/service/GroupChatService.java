package com.allen.imsystem.chat.service;

import com.allen.imsystem.chat.model.dto.ChatCacheDTO;
import com.allen.imsystem.chat.model.param.CreateGroupParam;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.model.vo.GroupMemberView;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.friend.model.param.InviteParam;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.chat.model.vo.GroupView;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 群会话相关的业务逻辑接口
 */
@Service
public interface GroupChatService {


    /**
     * 获取群聊会话的一些信息
     *
     * @param chatId 会话id
     * @return 用ChatSessionInfo封装
     */
    ChatSessionInfo getChatSessionInfo(long chatId, String uid);

    /**
     * 判断某个会话是否应该显示在会话列表（未被移除）
     *
     * @param uid 用户id
     * @param chatId 群id
     * @return 是否应该显示在会话列表
     */
    boolean isOpen(String uid, Long chatId);

    /**
     * 开启一个群聊会话
     *
     * @param uid uid
     * @param gid 群id
     */
    Map<String, Object> open(String uid, String gid);

    /**
     * 移除一个群聊会话
     * @param uid 用户uid
     * @param chatId 群聊chatId
     */
    void remove(String uid, Long chatId);

    /**
     * 获取该群的所有群会话，以Map的形式返回，Key: 群会话所属的用户id
     *
     * @param gid 群id
     * @return 该群的所有群会话，以Map的形式返回
     */
    Map<String, ChatSession> getAllGroupChatSession(String gid);

    /**
     * 更新群聊最后一条记录信息
     *
     * @param gid               群id
     * @param lastMsgId         最后一条消息id
     * @param lastMsgContent    消息内容
     * @param lastMsgCreateTime 消息创建时间
     * @param senderId          发送者id
     */
    void updateGroupLastMsg(String gid, Long lastMsgId, String lastMsgContent, Date lastMsgCreateTime,
                            String senderId);

    /**
     * 标识一个群聊会话的所有消息已读
     * @param uid 用户uid
     * @param gid 群gid
     */
    void setAllMsgHasRead(String uid, Long chatId);

    /**
     * 从群聊会话id获取gid
     *
     * @param chatId 群聊会话id
     * @return gid
     */
    String getGidFromChatId(Long chatId, String uid);

    ChatCacheDTO findChatCacheDTO(Long cahtId, String uid);
}
