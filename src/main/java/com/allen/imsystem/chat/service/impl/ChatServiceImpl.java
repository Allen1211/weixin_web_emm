package com.allen.imsystem.chat.service.impl;

import com.allen.imsystem.chat.mappers.PrivateChatMapper;
import com.allen.imsystem.chat.mappers.group.GroupChatMapper;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.chat.service.PrivateChatService;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.id.ChatIdUtil;
import com.allen.imsystem.message.service.impl.MessageCounter;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.allen.imsystem.common.Const.GlobalConst.ChatType;
import static com.allen.imsystem.common.Const.GlobalConst.RedisKey;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivateChatMapper privateChatMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PrivateChatService privateChatService;

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MessageCounter messageCounter;

    @Override
    public int getChatType(Long chatId) {
        return chatId == null ? ChatType.UN_KNOW : ChatIdUtil.getChatType(chatId);
    }

    @Override
    public Long getChatLastMsgTimestamp(Long chatId) {
        String val = (String) redisService.hget(RedisKey.KEY_CHAT_LAST_MSG_TIME, chatId.toString());
        if (val == null)
            return 0L;
        else
            return Long.valueOf(val);
    }

    @Override
    public void setChatLastMsgTimestamp(Long chatId, Long timestamp) {
        redisService.hset(RedisKey.KEY_CHAT_LAST_MSG_TIME,
                chatId.toString(), String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 标识一个会话的所有消息已读
     *
     * @param chatType 会话类型
     * @param uid      用户id
     * @param id       会话或是群的id，若为私聊会话，则为chatId, 若为群聊，则为gid
     */
    @Override
    public void setChatAllMsgHasRead(int chatType, String uid, String id) {

    }

    @Override
    public List<ChatSession> getChatList(String uid) {
        List<ChatSession> chatList = new ArrayList<>();
        // 获取私聊会话
        List<ChatSession> privateChatList = privateChatMapper.findChatSessionListByUid(uid);
        // 获取群聊会话
        List<ChatSession> groupChatList = groupChatMapper.selectGroupChatList(uid);
        if (!CollectionUtils.isEmpty(privateChatList)) {
            chatList.addAll(privateChatList);
        }
        if (!CollectionUtils.isEmpty(groupChatList)) {
            chatList.addAll(groupChatList);
        }

        if (!CollectionUtils.isEmpty(chatList)) {
            // 按更新时间倒序排序
            chatList.sort((o1, o2) -> {
                if (o1.getUpdateTime() == null) {
                    return -1;
                } else if (o2.getUpdateTime() == null) {
                    return 1;
                } else {
                    return o2.getUpdateTime().compareTo(o1.getUpdateTime());
                }
            });

            for (ChatSession chat : chatList) {
                Integer newMsgCount = null;
                // 填充会话基本信息
                if (chat.getIsGroupChat()) {
                    newMsgCount = messageCounter.getUserGroupChatNewMsgCount(uid, chat.getGid());
                } else {
                    UserInfoView userInfo = userService.findUserInfoDTO(chat.getFriendId());
                    chat.setAvatar(userInfo.getAvatar());
                    chat.setTalkTitle(userInfo.getUsername());
                    newMsgCount = messageCounter.getPrivateChatNewMsgCount(uid, chat.getChatId());
                }
                // 填充新消息条数
                chat.setNewMessageCount(newMsgCount == null ? 0 : newMsgCount);
                // 最后一条信息格式化
                if (chat.getLastMessage() == null || chat.getLastMessageDate() == null) {
                    chat.setLastMessage("");
                    chat.setLastMessageTime("");
                } else {
                    //日期时间格式化
                    chat.setLastMessageTime(FormatUtil.formatChatSessionDate(chat.getLastMessageDate()));
                }
            }
        }

        return chatList;
    }

    @Override
    public Map<String,Object> getChatInfo(Long chatId, String uid) {
        int chatType = getChatType(chatId);
        ChatSessionInfo chatSessionInfo = null;
        if (ChatType.PRIVATE_CHAT == chatType) {// 是私聊
            chatSessionInfo = privateChatService.getChatSessionInfo(chatId, uid);
        } else if (ChatType.GROUP_CHAT == chatType) {// 是群聊
            chatSessionInfo = groupChatService.getChatSessionInfo(chatId, uid);
        } else {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "获取会话类型失败,会话不存在");
        }
        Map<String,Object> resultMap = new HashMap<>(3);
        if(chatSessionInfo != null){
            resultMap.put("hasThisTalk", true);
            resultMap.put("hasInTalkList", chatSessionInfo.isOpen());
            resultMap.put("talkData", chatSessionInfo);
        }else{
            resultMap.put("hasThisTalk", false);
        }
        return resultMap;

    }

    /**
     * 更新私聊会话的最后一条信息
     *
     * @param chatType          会话类型
     * @param id                会话或是群的id，若为私聊会话，则为chatId, 若为群聊，则为gid
     * @param msgId             消息id
     * @param lastMsgContent    消息内容
     * @param lastMsgCreateTime 消息创建时间
     * @param senderId          发送者id
     */
    @Override
    public void updateChatLastMsg(int chatType, String id, Long msgId, String lastMsgContent,
                                  Date lastMsgCreateTime, String senderId) {
        if (chatType == ChatType.PRIVATE_CHAT) {
            privateChatService.updateLastMsg(Long.parseLong(id),msgId, lastMsgContent, lastMsgCreateTime, senderId);
        } else {
            groupChatService.updateGroupLastMsg(id, msgId, lastMsgContent, lastMsgCreateTime, senderId);
        }
    }

    @Override
    public Map<String, Object> validateChatId(Long chatId, String uid) {
        Map<String, Object> result = new HashMap<>(3);
        int chatType = getChatType(chatId);
        if (ChatType.UN_KNOW == chatType) {
            result.put("hasThisTalk", false);
            return result;
        }

        if (ChatType.PRIVATE_CHAT == chatType) {   // 若为私聊会话
            boolean hasInTalkList = privateChatService.isOpen(uid, chatId);
            result.put("hasInTalkList", hasInTalkList);
            PrivateChat privateChat = privateChatMapper.findByChatId(chatId);
            result.put("hasThisTalk", privateChat != null);
            // 存在此会话
            if (privateChat != null && !hasInTalkList) {
                ChatSession chatSession = privateChatMapper.selectNewMsgPrivateChatData(chatId, uid,
                        uid.equals(privateChat.getUidA()) ? privateChat.getUidB() : privateChat.getUidA());
                if (chatSession != null) {
                    chatSession.setNewMessageCount(messageCounter.getUserGroupChatNewMsgCount(uid, chatSession.getGid()));
                    result.put("payload", chatSession);
                }
            }
        } else if (ChatType.GROUP_CHAT == chatType) { //若为群聊会话
            ChatSession chatSession = groupChatMapper.selectOneGroupChatData(chatId);
            result.put("hasThisTalk", chatSession != null);
            if (chatSession != null) {
                boolean hasInTalkList = groupChatService.isOpen(uid, chatSession.getGid());
                result.put("hasInTalkList", hasInTalkList);
                if (!hasInTalkList) {
                    chatSession.setNewMessageCount(messageCounter.getUserGroupChatNewMsgCount(uid, chatSession.getGid()));
                    result.put("payload", chatSession);
                }
            }
        }
        return result;
    }

}
