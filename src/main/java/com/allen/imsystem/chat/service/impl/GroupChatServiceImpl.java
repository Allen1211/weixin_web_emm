package com.allen.imsystem.chat.service.impl;

import com.allen.imsystem.chat.mappers.GroupChatMapper;
import com.allen.imsystem.chat.mappers.GroupMapper;
import com.allen.imsystem.chat.model.dto.ChatCacheDTO;
import com.allen.imsystem.chat.model.pojo.Group;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.cache.CacheExecutor;
import com.allen.imsystem.common.cache.impl.ChatCache;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.message.service.impl.MessageCounter;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.allen.imsystem.common.Const.GlobalConst.RedisKey;

@Service
public class GroupChatServiceImpl implements GroupChatService {

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupChatMapper groupChatMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatCache chatCache;

    @Autowired
    private MessageCounter messageCounter;

    @Override
    public void updateGroupLastMsg(String gid, Long lastMsgId, String lastMsgContent, Date lastMsgCreateTime,
                                   String senderId) {
        groupMapper.updateGroupLastMsg(gid, lastMsgId, lastMsgContent, lastMsgCreateTime, senderId);
    }

    /**
     * 标识一个群聊会话的所有消息已读
     *
     * @param uid 用户uid
     * @param chatId 群gid
     */
    @Override
    public void setAllMsgHasRead(String uid, Long chatId) {
        // 有未读信息的话就才需要执行
        if(messageCounter.getPrivateChatNewMsgCount(uid, chatId) > 0){
            // 会话未读消息数清零
            messageCounter.setUserChatNewMsgCount(uid, chatId, 0);
            GroupChat groupChat = new GroupChat();
            groupChat.setChatId(chatId);
            groupChat.setUnreadMsgCount(0);
            groupChatMapper.update(groupChat);
        }
    }

    /**
     * 获取群聊会话的一些信息
     *
     * @param chatId 会话id
     * @param uid
     * @return 用ChatSessionInfo封装
     */
    @Override
    public ChatSessionInfo getChatSessionInfo(long chatId, String uid) {
        GroupChat groupChat = groupChatMapper.findByChatId(chatId);
        Group group = groupMapper.findByGId(groupChat.getGid());
        ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
        chatSessionInfo.setChatId(chatId);
        chatSessionInfo.setGid(group.getGid());
        chatSessionInfo.setSrcId(uid);
        chatSessionInfo.setDestId(group.getGid());
        chatSessionInfo.setTitle(group.getGroupName());
        chatSessionInfo.setGroupAlias(groupChat.getUserAlias());
        chatSessionInfo.setIsGroup(true);
        chatSessionInfo.setIsGroupOwner(uid.equals(group.getOwnerId()));
        UserInfoView userInfoView = userService.findUserInfoDTO(uid);
        chatSessionInfo.setAvatar(userInfoView.getAvatar());
        if(group.getLastMsgCreateTime() != null){
            chatSessionInfo.setLastTimeStamp(group.getLastMsgCreateTime().getTime());
        }
        chatSessionInfo.setOpen(groupChat.getShouldDisplay());
        return chatSessionInfo;
    }

    /**
     * 判断某个会话是否应该显示在会话列表（未被移除）
     *
     * @param uid 用户id
     * @param chatId 群id
     * @return 是否应该显示在会话列表
     */
    @Override
    public boolean isOpen(String uid, Long chatId) {
        if (chatId == null || uid == null) {
            return false;
        }
        return (boolean) CacheExecutor.get(chatCache.chatInfoCache,ChatCache.wrapChatInfoKey(chatId,uid),
                ChatCache.SHOULD_DISPLAY);
    }

    @Override
    public Map<String, ChatSession> getAllGroupChatSession(String gid) {
        return groupChatMapper.selectGroupAllChatData(gid);
    }

    @Override
    public String getGidFromChatId(Long chatId, String uid) {
        return (String) CacheExecutor.get(chatCache.chatInfoCache,ChatCache.wrapChatInfoKey(chatId,uid),ChatCache.GID);
    }

    @Override
    public ChatCacheDTO findChatCacheDTO(Long chatId, String uid) {
        GroupChat groupChat = groupChatMapper.findByChatId(chatId);
        if(groupChat == null){
            return null;
        }
        Group group = groupMapper.findByGId(groupChat.getGid());
        ChatCacheDTO chatCacheDTO = new ChatCacheDTO();
        chatCacheDTO.setUnreadMsgCount(groupChat.getUnreadMsgCount());
        if(group.getLastMsgCreateTime() != null){
            chatCacheDTO.setLastMsgTimestamp(group.getLastMsgCreateTime().getTime());
        }
        chatCacheDTO.setChatId(groupChat.getChatId());
        chatCacheDTO.setShouldDisplay(groupChat.getShouldDisplay());
        chatCacheDTO.setGroup(true);
        chatCacheDTO.setGid(group.getGid());
        return chatCacheDTO;
    }

    @Override
    public Map<String, Object> open(String uid, String gid) {
        GroupChat relation = groupChatMapper.findByUidGid(uid, gid);
        if (relation == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID);
        }
        Boolean isNewTalk = !relation.getShouldDisplay();
        if (isNewTalk) {
            relation.setShouldDisplay(true);
            relation.setUpdateTime(new Date());
            groupChatMapper.update(relation);
        }
        CacheExecutor.set(chatCache.chatInfoCache,ChatCache.wrapChatInfoKey(relation.getChatId(),relation.getUid()),
                ChatCache.SHOULD_DISPLAY, true);
        Map<String, Object> result = new HashMap<>(2);
        result.put("isNewTalk", isNewTalk);
        result.put("relation", relation);
        return result;
    }

    /**
     * 移除一个群聊会话
     *
     * @param uid    用户uid
     * @param chatId 群聊chatId
     */
    @Override
    public void remove(String uid, Long chatId) {
        GroupChat relation = groupChatMapper.findByChatId(chatId);
        if (relation == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "会话不存在");
        }
        // 设置该用户对该会话的移除为是
        CacheExecutor.set(chatCache.chatInfoCache,ChatCache.wrapChatInfoKey(relation.getChatId(),relation.getUid()),
                ChatCache.SHOULD_DISPLAY, false);
        // 会话未读消息数全部已读
        setAllMsgHasRead(uid, relation.getChatId());

        relation.setShouldDisplay(false);
        relation.setUpdateTime(new Date());
        groupChatMapper.update(relation);
    }


}
