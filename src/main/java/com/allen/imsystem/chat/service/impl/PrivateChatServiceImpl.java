package com.allen.imsystem.chat.service.impl;

import com.allen.imsystem.chat.mappers.PrivateChatMapper;
import com.allen.imsystem.chat.mappers.GroupChatMapper;
import com.allen.imsystem.chat.model.dto.ChatCacheDTO;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.chat.service.PrivateChatService;
import com.allen.imsystem.common.cache.CacheExecutor;
import com.allen.imsystem.common.cache.impl.ChatCache;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.friend.service.FriendQueryService;
import com.allen.imsystem.id.IdPoolService;
import com.allen.imsystem.message.mappers.GroupMsgRecordMapper;
import com.allen.imsystem.message.mappers.PrivateMsgRecordMapper;
import com.allen.imsystem.message.service.impl.MessageCounter;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.allen.imsystem.common.Const.GlobalConst.*;

/**
 * @ClassName PrivateChatServiceImpl
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/16
 * @Version 1.0
 */
@Service
public class PrivateChatServiceImpl implements PrivateChatService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivateChatMapper privateChatMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private PrivateMsgRecordMapper privateMsgRecordMapper;

    @Autowired
    private GroupMsgRecordMapper groupMsgRecordMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendQueryService friendService;

    @Autowired
    private MessageCounter messageCounter;

    @Autowired
    private IdPoolService idPoolService;

    @Autowired
    private ChatCache chatCache;

    /**
     * 判断某个会话是否应该显示在会话列表（未被移除）
     *
     * @param uid    用户id
     * @param chatId 会话id
     * @return 是否应该显示在会话列表
     */
    @Override
    public boolean isOpen(String uid, Long chatId) {
        if (chatId == null || uid == null) {
            return false;
        }
        return (boolean) CacheExecutor.get(chatCache.chatInfoCache, ChatCache.wrapChatInfoKey(chatId,uid),
                ChatCache.SHOULD_DISPLAY);
    }

    /**
     * 获取私聊会话的一些信息
     *
     * @param chatId 会话id
     * @return 用ChatSessionInfo封装
     */
    @Override
    public ChatSessionInfo getChatSessionInfo(long chatId, String uid) {
        PrivateChat privateChat = privateChatMapper.findByChatId(chatId);
        if (privateChat == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "该会话所对应的聊天不存在或已被删除");
        }
        ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
        // 自己
        UserInfoView selfInfoView = userService.findUserInfoDTO(uid);
        // 好友
        UserInfoView friendInfoView;
        if(uid.equals(privateChat.getUidA())){
            chatSessionInfo.setDestId(privateChat.getUidB());
            friendInfoView = userService.findUserInfoDTO(privateChat.getUidB());
            chatSessionInfo.setOpen(privateChat.getUserAStatus());
        }else{
            chatSessionInfo.setDestId(privateChat.getUidA());
            friendInfoView = userService.findUserInfoDTO(privateChat.getUidA());
            chatSessionInfo.setOpen(privateChat.getUserBStatus());
        }
        chatSessionInfo.setAvatar(selfInfoView.getAvatar());
        chatSessionInfo.setSrcId(uid);
        chatSessionInfo.setIsGroupOwner(false);
        chatSessionInfo.setIsGroup(false);
        chatSessionInfo.setChatId(chatId);
        chatSessionInfo.setTitle(friendInfoView.getUsername());
        chatSessionInfo.setIsGroupOwner(false);
        if(privateChat.getLastMsgCreateTime() != null){
            chatSessionInfo.setLastTimeStamp(privateChat.getLastMsgCreateTime().getTime());
        }
        return chatSessionInfo;
    }

    /**
     * 初始化一个私聊会话
     *
     * @param uid      用户id
     * @param friendId 好友id
     * @param status   会话是否显示在会话列表
     * @return 私聊会话实体类
     */
    @Override
    public PrivateChat init(String uid, String friendId, Boolean status) {
        Long chatId = idPoolService.nextChatId(ChatType.PRIVATE_CHAT);
        String uidA = getUidAUidB(uid, friendId)[0];
        String uidB = getUidAUidB(uid, friendId)[1];
        PrivateChat privateChat = new PrivateChat();
        privateChat.setChatId(chatId);
        privateChat.setUidA(uidA);
        privateChat.setUidB(uidB);
        if (uidA.equals(uid)) {
            privateChat.setUserAStatus(status);
        } else {
            privateChat.setUserBStatus(status);
        }
        privateChat.setCreatedTime(new Date());
        privateChat.setUpdateTime(new Date());
        privateChatMapper.insert(privateChat);
        return privateChat;
    }

    /**
     * 开启一个对好友的私聊会话
     *
     * @param uid      用户id
     * @param friendId 好友id
     */
    @Override
    public Map<String, Object> open(String uid, String friendId) {
        String uidA = getUidAUidB(uid, friendId)[0];
        String uidB = getUidAUidB(uid, friendId)[1];

        // 获取会话信息
        PrivateChat privateChat = privateChatMapper.findByUidAB(uidA, uidB);

        //判断对方是否已经把自己删掉
        Boolean isDeletedByFriend = friendService.checkIsDeletedByFriend(uid, friendId);
        if (isDeletedByFriend) {   // 被删了
            if (privateChat != null) {    // 有会话信息，可以跳转
                return doOpenPrivateChat(uid, friendId, privateChat);
            } else { // 无会话信息，异常
                throw new BusinessException(ExceptionType.TALK_NOT_VALID, "被好友删除，但会话信息也不存在，数据异常");
            }
        } else if (privateChat == null) {  // 没被删，而且没有会话信息，为这对好友新建一个会话
            Map<String, Object> result = new HashMap<>(2);
            result.put("privateChat", init(uid, friendId, true));
            result.put("isNewTalk", true);
            return result;
        } else {
            // 没被删，有会话信息，可以跳转，更新会话对当前用户有效
            return doOpenPrivateChat(uid, friendId, privateChat);
        }

    }

    private Map<String, Object> doOpenPrivateChat(String uid, String friendId, PrivateChat privateChat) {
        String uidA = getUidAUidB(uid, friendId)[0];
        boolean isNewTalk;
        if (uidA.equals(uid)) {
            isNewTalk = !privateChat.getUserAStatus();
            privateChat.setUserAStatus(true);
        } else {
            isNewTalk = !privateChat.getUserBStatus();
            privateChat.setUserBStatus(true);
        }

        // 更新数据库
        privateChatMapper.update(privateChat);
        // 设置该用户对该会话的移除为否
        CacheExecutor.remove(chatCache.chatInfoCache,ChatCache.wrapChatInfoKey(privateChat.getChatId(),uid),
                ChatCache.SHOULD_DISPLAY);
        // 初始化该用户对此会话未读消息数
        messageCounter.setUserChatNewMsgCount(uid, privateChat.getChatId(), 0);
        Map<String, Object> result = new HashMap<>(2);
        result.put("privateChat", privateChat);
        result.put("isNewTalk", isNewTalk);
        return result;
    }

    /**
     * 移除一个对好友的私聊会话
     *
     * @param uid    用户id
     * @param chatId 与好友的会话id
     */
    @Override
    @Transactional
    public void remove(String uid, Long chatId) {
        // 设置该用户对该会话的移除为是
        CacheExecutor.remove(chatCache.chatInfoCache,ChatCache.wrapChatInfoKey(chatId,uid),
                ChatCache.SHOULD_DISPLAY);
        // 会话未读消息数清零
        messageCounter.setUserChatNewMsgCount(uid, chatId, 0);
        PrivateChat privateChat = privateChatMapper.findByChatId(chatId);
        //
        if (uid.equals(privateChat.getUidA())) {
            privateChat.setUserAStatus(false);
        } else {
            privateChat.setUserBStatus(false);
        }
        privateChatMapper.update(privateChat);
        // 该会话所有历史消息设为已读
        setAllMsgHasRead(uid, privateChat.getChatId());
    }

    /**
     * 移除一个对好友的私聊会话
     *
     * @param uid      用户id
     * @param friendId 好友id
     */
    @Override
    @Transactional
    public void remove(String uid, String friendId) {
        String uidA = getUidAUidB(uid, friendId)[0];
        String uidB = getUidAUidB(uid, friendId)[1];

        PrivateChat privateChat = privateChatMapper.findByUidAB(uidA, uidB);
        if (privateChat == null) {
            return;
        }
        if (uid.equals(privateChat.getUidA())) {
            privateChat.setUserAStatus(false);
        } else {
            privateChat.setUserBStatus(false);
        }
        privateChatMapper.update(privateChat);
        // 设置该用户对该会话的移除为是
        CacheExecutor.remove(chatCache.chatInfoCache,ChatCache.wrapChatInfoKey(privateChat.getChatId(),uid),
                ChatCache.SHOULD_DISPLAY);         // 该会话所有历史消息设为已读
        setAllMsgHasRead(uid, privateChat.getChatId());
    }

    /**
     * 标识一个私聊会话的所有消息已读
     *
     * @param uid    用户id
     * @param chatId 会话id
     */
    @Override
    public void setAllMsgHasRead(String uid, Long chatId) {
        // 有未读信息的话就才需要执行
        if(messageCounter.getPrivateChatNewMsgCount(uid, chatId) > 0){
            // 会话未读消息数清零
            messageCounter.setUserChatNewMsgCount(uid, chatId, 0);
            privateMsgRecordMapper.setAllPrivateChatMsgHasRead(chatId, uid);
        }
    }

    /**
     * 更新私聊会话的最后一条信息
     *
     * @param chatId            会话id
     * @param msgId             消息id
     * @param lastMsgContent    消息内容
     * @param lastMsgCreateTime 消息创建时间
     * @param senderId          发送者id
     */
    @Override
    public void updateLastMsg(Long chatId, Long msgId, String lastMsgContent, Date lastMsgCreateTime, String senderId) {
        privateChatMapper.updatePrivateChatLastMsg(chatId, msgId, lastMsgContent, lastMsgCreateTime, senderId);
    }

    @Override
    public ChatCacheDTO findChatCacheDTO(Long chatId, String uid) {
        PrivateChat privateChat = privateChatMapper.findByChatId(chatId);
        if(privateChat == null){
            return null;
        }
        ChatCacheDTO chatCacheDTO = new ChatCacheDTO();
        chatCacheDTO.setChatId(privateChat.getChatId());
        if(privateChat.getLastMsgCreateTime() != null){
            chatCacheDTO.setLastMsgTimestamp(privateChat.getLastMsgCreateTime().getTime());
        }
        if(uid.equals(privateChat.getUidA())){
            chatCacheDTO.setUnreadMsgCount(privateChat.getUserAUnreadMsgCount());
            chatCacheDTO.setShouldDisplay(privateChat.getUserAStatus());
        }else{
            chatCacheDTO.setUnreadMsgCount(privateChat.getUserBUnreadMsgCount());
            chatCacheDTO.setShouldDisplay(privateChat.getUserBStatus());
        }
        chatCacheDTO.setGroup(false);
        return chatCacheDTO;
    }

    private String[] getUidAUidB(String uid, String friendId) {
        String uidA = uid.compareTo(friendId) < 0 ? uid : friendId;
        String uidB = uid.compareTo(friendId) < 0 ? friendId : uid;
        return new String[]{uidA, uidB};
    }
}
