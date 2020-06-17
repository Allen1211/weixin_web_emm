package com.allen.imsystem.chat.service.impl;

import com.allen.imsystem.chat.mappers.group.GroupChatMapper;
import com.allen.imsystem.chat.mappers.group.GroupMapper;
import com.allen.imsystem.chat.model.pojo.Group;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.friend.service.FriendQueryService;
import com.allen.imsystem.id.IdPoolService;
import com.allen.imsystem.message.mappers.GroupMsgRecordMapper;
import com.allen.imsystem.message.service.MessageService;
import com.allen.imsystem.user.mappers.UserMapper;
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

    @Override
    public void updateGroupLastMsg(String gid, Long lastMsgId, String lastMsgContent, Date lastMsgCreateTime,
                                   String senderId) {
        redisService.hset(RedisKey.KEY_CHAT_LAST_MSG_TIME, gid, String.valueOf(System.currentTimeMillis()));
        groupMapper.updateGroupLastMsg(gid, lastMsgId, lastMsgContent, lastMsgCreateTime, senderId);
    }

    /**
     * 标识一个群聊会话的所有消息已读
     *
     * @param uid 用户uid
     * @param gid 群gid
     */
    @Override
    public void setAllMsgHasRead(String uid, String gid) {
        redisService.hset(RedisKey.KEY_CHAT_UNREAD_COUNT, uid + gid, 0L);
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
     * @param gid 群id
     * @return 是否应该显示在会话列表
     */
    @Override
    public boolean isOpen(String uid, String gid) {
        if (gid == null || uid == null) {
            return false;
        }
        Boolean isRemove = (Boolean) redisService.hget(RedisKey.KEY_CHAT_REMOVE, uid + gid);
        return isRemove != null && !isRemove;
    }

    @Override
    public Map<String, ChatSession> getAllGroupChatSession(String gid) {
        return groupChatMapper.selectGroupAllChatData(gid);
    }


    @Override
    public String getGidFromChatId(Long chatId) {
        String key = RedisKey.KEY_CHAT_GID_MAP + chatId.toString();
        if (!redisService.hasKey(key)) {
            String gid = groupChatMapper.selectGidFromChatId(chatId);
            if (gid != null) {
                redisService.set(key, gid, 10L, TimeUnit.MINUTES);
            } else {
                throw new BusinessException(ExceptionType.SERVER_ERROR);
            }
        }
        return (String) redisService.get(key);
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
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, uid + gid, false);
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
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, uid + relation.getGid(), true);
        // 会话未读消息数全部已读
        setAllMsgHasRead(uid, relation.getGid());

        relation.setShouldDisplay(false);
        relation.setUpdateTime(new Date());
        groupChatMapper.update(relation);
    }


}
