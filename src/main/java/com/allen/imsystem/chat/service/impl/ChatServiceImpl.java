package com.allen.imsystem.chat.service.impl;

import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.model.vo.ChatSessionInfo;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.file.model.MsgFileInfo;
import com.allen.imsystem.id.IdPoolService;
import com.allen.imsystem.common.bean.PageBean;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.id.ChatIdUtil;
import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.friend.service.FriendQueryService;
import com.allen.imsystem.chat.mappers.PrivateChatMapper;
import com.allen.imsystem.chat.mappers.group.GroupChatMapper;
import com.allen.imsystem.message.mappers.GroupMsgRecordMapper;
import com.allen.imsystem.message.mappers.PrivateMsgRecordMapper;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.chat.model.pojo.PrivateChat;
import com.allen.imsystem.message.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.chat.model.pojo.GroupChat;
import com.allen.imsystem.message.service.impl.MessageCounter;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.allen.imsystem.common.Const.GlobalConst.*;

@Service
public class ChatServiceImpl implements ChatService {

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
    private GroupChatService groupChatService;

    @Autowired
    private FileService fileService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MessageCounter messageCounter;

    @Autowired
    private IdPoolService idPoolService;

    @Override
    public boolean isGroupChat(Long chatId) {
        Integer chatType = getChatType(chatId);
        return ChatType.GROUP_CHAT.equals(chatType);
    }

    @Override
    public Integer getChatType(Long chatId) {
        return chatId == null ? ChatType.UN_KNOW : ChatIdUtil.getChatType(chatId);
    }

    @Override
    public Integer getChatType(String chatId) {
        if (StringUtils.isNotEmpty(chatId) && StringUtils.isNumeric(chatId)) {
            return this.getChatType(Long.parseLong(chatId));
        } else {
            return ChatType.UN_KNOW;
        }
    }

    @Override
    public Boolean isPrivateChatSessionOpenToUser(String uid, Long chatId) {
        if (chatId == null || uid == null) {
            return false;
        }
        Boolean isRemove = (Boolean) redisService.hget(RedisKey.KEY_CHAT_REMOVE, uid + chatId);
        return isRemove != null && !isRemove;
    }

    @Override
    public Boolean isGroupChatSessionOpenToUser(String uid, String gid) {
        if (gid == null || uid == null) {
            return false;
        }
        Boolean isRemove = (Boolean) redisService.hget(RedisKey.KEY_CHAT_REMOVE, uid + gid);
        return isRemove != null && !isRemove;
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
    public boolean setChatLastMsgTimestamp(Long chatId, Long timestamp) {
        return redisService.hset(RedisKey.KEY_CHAT_LAST_MSG_TIME,
                chatId.toString(), String.valueOf(System.currentTimeMillis()));
    }


    @Transactional
    public PrivateChat initNewPrivateChat(String uid, String friendId, Boolean status) {
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
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, uid + chatId, !status);
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, friendId + chatId, true);
        messageCounter.setUserChatNewMsgCount(uid, chatId, 0);   // 初始化该用户对此会话未读消息数
        messageCounter.setUserChatNewMsgCount(friendId, chatId, 0);   // 初始化该用户对此会话未读消息数
        return privateChat;
    }


    @Override
    @Transactional
    public Map<String, Object> openNewPrivateChat(String uid, String friendId) {
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
            result.put("privateChat", initNewPrivateChat(uid, friendId, true));
            result.put("isNewTalk", true);
            return result;
        } else {
            // 没被删，有会话信息，可以跳转，更新会话对当前用户有效
            return doOpenPrivateChat(uid, friendId, privateChat);
        }

    }

    @Override
    public Map<String, Object> openGroupChat(String uid, String gid) {
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
        String userChatRemoveKey = uid + privateChat.getChatId();
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, userChatRemoveKey, false);
        // 初始化该用户对此会话未读消息数
        messageCounter.setUserChatNewMsgCount(uid, privateChat.getChatId(), 0);
        Map<String, Object> result = new HashMap<>(2);
        result.put("privateChat", privateChat);
        result.put("isNewTalk", isNewTalk);
        return result;
    }

    @Override
    @Transactional
    public void removePrivateChat(String uid, Long chatId) {
        // 设置该用户对该会话的移除为是
        String userChatRemoveKey = uid + chatId;
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, userChatRemoveKey, true);
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
        setPrivateChatAllMsgHasRead(uid, privateChat.getChatId());
    }


    @Override
    public void removeGroupChat(String uid, Long chatId) {
        GroupChat relation = groupChatMapper.findByChatId(chatId);
        if (relation == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "会话不存在");
        }
        // 设置该用户对该会话的移除为是
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, uid + relation.getGid(), true);
        // 会话未读消息数全部已读
        setGroupChatAllMsgHasRead(uid, relation.getGid());

        relation.setShouldDisplay(false);
        relation.setUpdateTime(new Date());
        groupChatMapper.update(relation);
    }

    @Override
    @Transactional
    public void removePrivateChat(String uid, String friendId) {
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
        String userChatRemoveKey = uid + privateChat.getChatId();
        redisService.hset(RedisKey.KEY_CHAT_REMOVE, userChatRemoveKey, true);
        // 该会话所有历史消息设为已读
        setPrivateChatAllMsgHasRead(uid, privateChat.getChatId());
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
                if(chat.getIsGroupChat()){
                    newMsgCount = messageCounter.getUserGroupChatNewMsgCount(uid,chat.getGid());
                }else{
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
    public ChatSessionInfo getChatInfo(Long chatId, String uid) {
        Integer chatType = getChatType(chatId);
        if (chatType == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "获取会话类型失败,会话不存在");
        }
        String avatar = userMapper.selectUserInfoDTO(uid).getAvatar();
        ChatSessionInfo chatSessionInfo = null;
        if (ChatType.PRIVATE_CHAT.equals(chatType)) {// 是私聊
            chatSessionInfo = privateChatMapper.selectPrivateChatData(chatId, uid);
            if (chatSessionInfo == null) {
                throw new BusinessException(ExceptionType.TALK_NOT_VALID, "该会话所对应的聊天不存在或已被删除");
            }
            chatSessionInfo.setAvatar(avatar);
            chatSessionInfo.setIsGroupOwner(false);
            chatSessionInfo.setLastTimeStamp(getChatLastMsgTimestamp(chatId));
        } else if (ChatType.GROUP_CHAT.equals(chatType)) {// 是群聊
            chatSessionInfo = groupChatMapper.selectGroupChatData(chatId);
            if (chatSessionInfo == null) {
                throw new BusinessException(ExceptionType.TALK_NOT_VALID, "该会话所对应的聊天不存在或已被删除");
            }
            chatSessionInfo.setAvatar(avatar);
            chatSessionInfo.setIsGroupOwner(uid.equals(chatSessionInfo.getSrcId()));
            chatSessionInfo.setSrcId(uid);
            chatSessionInfo.setLastTimeStamp(getChatLastMsgTimestamp(Long.parseLong(chatSessionInfo.getGid())));
        }
        return chatSessionInfo;

    }

    @Override
    public Boolean setPrivateChatAllMsgHasRead(String uid, Long chatId) {
        Integer chatType = getChatType(chatId);
        if (chatType == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "获取会话类型失败,会话不存在");
        }
        // 会话未读消息数清零
        messageCounter.setUserChatNewMsgCount(uid, chatId, 0);

        if (chatType.equals(ChatType.PRIVATE_CHAT)) {
            return privateMsgRecordMapper.setAllPrivateChatMsgHasRead(chatId, uid) > 0;
        }
        return true;
    }

    @Override
    public Boolean setGroupChatAllMsgHasRead(String uid, String gid) {
        return redisService.hset(RedisKey.KEY_CHAT_UNREAD_COUNT, uid + gid, 0L);
    }

    @Override
    public Map<String, Object> getMessageRecord(boolean isGroup, String uid, Long chatId, Integer index, Integer pageSize) {
        Map<String, Object> resultMap = new HashMap<>(3);
        List<MsgRecord> messageList = null;
        // 如果是第一页，要获取一次总页数，记录一下统计的起始时间
        if (index == 1) {
            long now = System.currentTimeMillis();
            if (isGroup) {
                // 如果已经不是群成员了，只显示之前的聊天记录，不显示最新的
                GroupChat relation = groupChatMapper.findByChatId(chatId);
                if (!relation.getStatus()) {
                    now = relation.getUpdateTime().getTime();
                }
            }
            Integer totalSize = this.getAllHistoryMessageSize(isGroup, chatId, uid, new Date(now));
            int totalPage = 1;
            if (totalSize <= pageSize) {
                totalPage = 1;
            } else if (totalSize % pageSize == 0) {
                totalPage = totalSize / pageSize;
            } else {
                totalPage = totalSize / pageSize + 1;
            }
            messageList = doGetMessageList(isGroup, uid, chatId, null, index, pageSize);
            if (!CollectionUtils.isEmpty(messageList)) {
                Long latestMsgId = messageList.get(messageList.size() - 1).getMessageId();
                redisService.hset(RedisKey.KEY_RECORD_BEGIN_ID, chatId.toString(), latestMsgId.toString());
            }
            resultMap.put("messageList", messageList);
            resultMap.put("allPageSize", totalPage);
            resultMap.put("curPageIndex", index);
        } else {
            String beginMsgIdStr = (String) redisService.hget(RedisKey.KEY_RECORD_BEGIN_ID, chatId.toString());
            Long beginMsgId = null;
            if (StringUtils.isNotEmpty(beginMsgIdStr)) {
                beginMsgId = Long.parseLong(beginMsgIdStr);
            }
            messageList = doGetMessageList(isGroup, uid, chatId, beginMsgId, index, pageSize);
            resultMap.put("messageList", messageList);
            resultMap.put("curPageIndex", index);
        }

        return resultMap;
    }

    private List<MsgRecord> doGetMessageList(boolean isGroup, String uid, Long chatId, Long beginMsgId, Integer index, Integer pageSize) {

        PageBean pageBean = new PageBean(index, pageSize);
        List<MsgRecord> msgRecordList;

        if (isGroup) {
            String gid = groupChatService.getGidFromChatId(chatId);
            msgRecordList = groupMsgRecordMapper.selectGroupChatHistoryMsg(gid, beginMsgId, uid, pageBean);
        } else {
            msgRecordList =
                    privateMsgRecordMapper.findMsgRecordList(chatId, beginMsgId, uid, pageBean);
        }

        if (msgRecordList == null) {
            return new ArrayList<>();
        }
        Long preMsgTime = null;
        for (int i = msgRecordList.size() - 1; i >= 0; i--) {
            MsgRecord msgRecord = msgRecordList.get(i);
            // 是否显示
            msgRecord.setShowMessage(true);

            // 获取发送者的用户信息
            UserInfoView userInfo = userService.findUserInfoDTO(msgRecord.getFromUid());
            msgRecord.setUserInfo(userInfo);

            if (msgRecord.getMessageType() != 4) {
                // 是否是自己发的
                msgRecord.setUserType(
                        uid.equals(msgRecord.getUserInfo().getUid()) ? 1 : 0
                );
            }

            // 消息类型
            switch (msgRecord.getMessageType()) {
                case 4: //群通知同普通文本
                case 1: {// 普通文本
                    msgRecord.setFileInfo(null);
                    msgRecord.setMessageImgUrl(null);
                    break;
                }
                case 2: {// 图片
                    msgRecord.setMessageText("[图片]");
                    MsgFileInfo fileInfo = msgRecord.getFileInfo();
                    String imgUrl;
                    if (fileInfo == null) {
                        imgUrl = Path.IMG_NOT_FOUND;
                    } else {
                        imgUrl = fileInfo.getDownloadUrl();
                    }
                    msgRecord.setMessageImgUrl(imgUrl == null ? Path.IMG_NOT_FOUND : imgUrl);
                    break;
                }
                case 3: {// 文件
                    msgRecord.setMessageImgUrl(null);
                    MsgFileInfo fileInfo = msgRecord.getFileInfo();
                    if (fileInfo == null) {
                        msgRecord.setFileInfo(new MsgFileInfo("不存在的文件", ""));
                        msgRecord.getFileInfo().setFileSize("0");
                    } else {
                        String fileSize = FormatUtil.formatFileSize(fileInfo.getSize());
                        msgRecord.getFileInfo().setFileSize(fileSize);
                    }
                    break;
                }
            }

            // 发送的时间处理
            // 首条或者相差五分钟的才显示时间
            Long thisMsgTime = msgRecord.getMsgTimeDate().getTime();
            boolean showMsgTime = preMsgTime == null || (thisMsgTime - preMsgTime >= MAX_NOT_SHOW_TIME_SPACE);
            msgRecord.setShowMessageTime(showMsgTime);
            if (showMsgTime) {
                // 时间格式化处理
                String format = FormatUtil.formatMessageDate(msgRecord.getMsgTimeDate());
                msgRecord.setMessageTime(format);
            }
            preMsgTime = msgRecord.getMsgTimeDate().getTime();

        }
        Collections.reverse(msgRecordList);
        return msgRecordList;
    }


    @Override
    public Integer getAllHistoryMessageSize(boolean isGroup, Long chatId, String uid, Date beginTime) {
        Integer totalSize = null;
        if (isGroup) {
            String gid = groupChatService.getGidFromChatId(chatId);
            totalSize = groupMsgRecordMapper.countAllGroupHistoryMsg(gid, beginTime);
        } else {
            totalSize = privateMsgRecordMapper.countAllPrivateHistoryMsg(chatId, beginTime);
        }
        return totalSize == null ? 0 : totalSize;
    }

    @Override
    public PrivateMsgRecord savePrivateMsgRecord(SendMsgDTO msg) {
        PrivateMsgRecord privateMsgRecord = new PrivateMsgRecord();
        switch (msg.getMessageType()) {
            case 1: {    // 文字消息
                privateMsgRecord.setContent(msg.getMessageText());
                privateMsgRecord.setResourceUrl("");
                break;
            }
            case 2: {    // 图片消息
                privateMsgRecord.setContent("[图片]");
                privateMsgRecord.setResourceUrl(msg.getMessageImgUrl());
                String imgUrl = msg.getMessageImgUrl();
                if (StringUtils.isEmpty(imgUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                privateMsgRecord.setFileMd5(fileService.getMd5FromUrl(imgUrl));
                break;
            }
            case 3: {    // 文件消息
                privateMsgRecord.setContent(msg.getFileInfo().getFileName());
                privateMsgRecord.setResourceUrl(msg.getFileInfo().getDownloadUrl());
                String fileUrl = msg.getFileInfo().getDownloadUrl();
                if (StringUtils.isEmpty(fileUrl)) {
                    throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL);
                }
                privateMsgRecord.setFileMd5(fileService.getMd5FromUrl(fileUrl));
                break;
            }
        }

        privateMsgRecord.setMsgId(msg.getMsgId());
        privateMsgRecord.setChatId(Long.valueOf(msg.getTalkId()));
        privateMsgRecord.setFromUid(msg.getSrcId());
        privateMsgRecord.setToUid(msg.getDestId());
        privateMsgRecord.setHasRead(false);
        privateMsgRecord.setMsgType(msg.getMessageType());
        privateMsgRecord.setStatus(true);
        Date msgTime = new Date(Long.parseLong(msg.getTimeStamp()));
        privateMsgRecord.setCreatedTime(msgTime);
        privateMsgRecord.setUpdateTime(msgTime);
        privateMsgRecordMapper.insert(privateMsgRecord);

        return privateMsgRecord;
    }

    @Override
    public boolean updateChatLastMsg(Long chatId, Long msgId, String lastMsgContent,
                                     Date lastMsgCreateTime, String senderId) {
        return privateChatMapper.updatePrivateChatLastMsg(chatId, msgId, lastMsgContent, lastMsgCreateTime, senderId) > 0;
    }

    @Override
    public Map<String, Object> validateChatId(Long chatId, String uid) {
        Map<String, Object> result = new HashMap<>(3);
        Integer chatType = getChatType(chatId);
        if (ChatType.UN_KNOW.equals(chatType)) {
            result.put("hasThisTalk", false);
            return result;
        }

        if (ChatType.PRIVATE_CHAT.equals(chatType)) {   // 若为私聊会话
            boolean hasInTalkList = isPrivateChatSessionOpenToUser(uid, chatId);
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
        } else if (ChatType.GROUP_CHAT.equals(chatType)) { //若为群聊会话
            ChatSession chatSession = groupChatMapper.selectOneGroupChatData(chatId);
            result.put("hasThisTalk", chatSession != null);
            if (chatSession != null) {
                boolean hasInTalkList = isGroupChatSessionOpenToUser(uid, chatSession.getGid());
                result.put("hasInTalkList", hasInTalkList);
                if (!hasInTalkList) {
                    chatSession.setNewMessageCount(messageCounter.getUserGroupChatNewMsgCount(uid, chatSession.getGid()));
                    result.put("payload", chatSession);
                }
            }
        }
        return result;
    }


    private String[] getUidAUidB(String uid, String friendId) {
        String uidA = uid.compareTo(friendId) < 0 ? uid : friendId;
        String uidB = uid.compareTo(friendId) < 0 ? friendId : uid;
        return new String[]{uidA, uidB};
    }

}
