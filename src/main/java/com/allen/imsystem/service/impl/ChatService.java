package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.PageBean;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.DateFomatter;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.dao.ChatDao;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.dao.mappers.UserMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.ChatGroup;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IFriendService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ChatService implements IChatService {

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private RedisService redisService;

    @Override
    public Integer getChatType(String talkIdStr) {
        return (Integer) redisService.hget(GlobalConst.Redis.KEY_CHAT_TYPE, talkIdStr);
    }

    @Override
    public Integer getChatType(Long talkId) {
        return getChatType(String.valueOf(talkId));
    }

    @Override
    public Boolean isChatSessionOpenToUser(String uid, Long chatId) {
        if (chatId == null || uid == null) {
            return false;
        }
        Boolean isRemove = (Boolean) redisService.hget(GlobalConst.Redis.KEY_CHAT_REMOVE, uid + chatId);
        return isRemove != null && !isRemove;
    }

    @Override
    public Boolean isChatSessionOpenToUser(String uid, String chatId) {
        return chatId != null && uid != null && isChatSessionOpenToUser(uid, Long.valueOf(chatId));
    }

    @Override
    public Long getChatLastMsgTimestamp(Long chatId) {
        String val = (String) redisService.hget(GlobalConst.Redis.KEY_CHAT_LAST_MSG_TIME, chatId.toString());
        if (val == null)
            return 0L;
        else
            return Long.valueOf(val);
    }

    @Override
    public boolean setChatLastMsgTimestamp(Long chatId, Long timestamp) {
        return redisService.hset(GlobalConst.Redis.KEY_CHAT_LAST_MSG_TIME,
                chatId.toString(), String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public boolean setUserChatNewMsgCount(String uid, Long chatId, Integer count) {
        if(count == null) count = 0;
        return redisService.hset(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, uid+chatId, count);
    }

    @Override
    public Integer getUserChatNewMsgCount(String uid, Long chatId) {
        Integer count = (Integer) redisService.hget(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT, uid+chatId);
        if(count == null){  // 如果缓存中不存在，到数据库里查询，并更新缓存
            Integer newMsgCount = chatMapper.countPrivateChatUnReadMsgForUser(chatId,uid);
            setUserChatNewMsgCount(uid,chatId,newMsgCount);
            return newMsgCount;
        }
        return count == null ? 0: count;
    }

    @Override
    public void incrUserChatNewMsgCount(String uid, Long chatId) {
        boolean hasKey = redisService.hHasKey(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT,uid+chatId);
        if(!hasKey){
            // 去数据库里同步。
            Integer newMsgCount = chatMapper.countPrivateChatUnReadMsgForUser(chatId,uid);
            setUserChatNewMsgCount(uid,chatId,newMsgCount!=null ? newMsgCount+1 : 1);
            return;
        }
        Long val = redisService.hincr(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT,uid+chatId,1L);
        return;
    }

    @Transactional
    public PrivateChat initNewPrivateChat(String uid, String friendId, Boolean status) {
        Long chatId = SnowFlakeUtil.getNextSnowFlakeId();
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
        chatMapper.insertNewPrivateChat(privateChat);
        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, uid + chatId, !status);
        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, friendId + chatId, true);
        redisService.hset(GlobalConst.Redis.KEY_CHAT_TYPE, chatId.toString(), GlobalConst.ChatType.PRIVATE_CHAT);
        setUserChatNewMsgCount(uid,chatId,0);   // 初始化该用户对此会话未读消息数
        setUserChatNewMsgCount(friendId,chatId,0);   // 初始化该用户对此会话未读消息数
        return privateChat;
    }


    @Override
    @Transactional
    public Map<String, java.lang.Object> openNewPrivateChat(String uid, String friendId) {
        String uidA = getUidAUidB(uid, friendId)[0];
        String uidB = getUidAUidB(uid, friendId)[1];

        // 获取会话信息
        PrivateChat privateChat = chatMapper.selectPrivateChatInfoByUid(uidA, uidB);

        //判断对方是否已经把自己删掉
        Boolean isDeletedByFriend = friendService.checkIsDeletedByFriend(uid, friendId);
//        if(isDeletedByFriend){  // 对方已经把自己删除，开启新对话应提示
//            throw new BusinessException(ExceptionType.CANNOT_OPEN_PRIVATE_CHAT);
//        }
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
        }else{
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
        chatMapper.updatePrivateChat(privateChat);
        // 设置该用户对该会话的移除为否
        String userChatRemoveKey = uid + privateChat.getChatId();
        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, userChatRemoveKey, false);
        // 会话类型
        redisService.hset(GlobalConst.Redis.KEY_CHAT_TYPE, privateChat.getChatId().toString(), GlobalConst.ChatType.PRIVATE_CHAT);
        // 初始化该用户对此会话未读消息数
        setUserChatNewMsgCount(uid,privateChat.getChatId(),0);
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
        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, userChatRemoveKey, true);
        // 会话未读消息数清零
        setUserChatNewMsgCount(uid,chatId,0);
        PrivateChat privateChat = chatMapper.selectPrivateChatInfoByChatId(chatId.toString());
        //
        if (uid.equals(privateChat.getUidA())) {
            privateChat.setUserAStatus(false);
        } else {
            privateChat.setUserBStatus(false);
        }
        chatMapper.updatePrivateChat(privateChat);
        // 该会话所有历史消息设为已读
        setTalkAllMsgHasRead(uid,privateChat.getChatId().toString());
    }

    @Override
    @Transactional
    public void removePrivateChat(String uid, String friendId) {
        String uidA = getUidAUidB(uid, friendId)[0];
        String uidB = getUidAUidB(uid, friendId)[1];

        PrivateChat privateChat = chatMapper.selectPrivateChatInfoByUid(uidA, uidB);
        if (uid.equals(privateChat.getUidA())) {
            privateChat.setUserAStatus(false);
        } else {
            privateChat.setUserBStatus(false);
        }
        chatMapper.updatePrivateChat(privateChat);
        // 设置该用户对该会话的移除为是
        String userChatRemoveKey = uid + privateChat.getChatId();
        redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, userChatRemoveKey, true);
        // 该会话所有历史消息设为已读
        setTalkAllMsgHasRead(uid,privateChat.getChatId().toString());
    }

    @Override
    public List<ChatSessionDTO> getChatList(String uid) {
        // 获取私聊会话
        List<ChatSessionDTO> privateChatList = chatDao.selectPrivateChatList(uid);
        if (privateChatList != null && privateChatList.size() > 0) {
            Map<Long, ChatNewMsgSizeDTO> sizeMap = chatDao.selectPrivateChatNewMsgSize(privateChatList, uid);
            for (int i = 0; i < privateChatList.size(); i++) {
                ChatSessionDTO privateChat = privateChatList.get(i);

                // 从数据库， 填充新消息条数
//                ChatNewMsgSizeDTO sizeDTO = sizeMap.get(privateChat.getTalkId());
//                privateChat.setNewMessageCount(sizeDTO == null ? 0 : sizeDTO.getSize());
                Integer newMsgCount = (Integer) redisService.hget(GlobalConst.Redis.KEY_CHAT_UNREAD_COUNT,
                        uid+privateChat.getTalkId().toString());
                privateChat.setNewMessageCount(newMsgCount==null?0:newMsgCount);
                // 填充在线信息
                Integer onlineStatus = userService.getUserOnlineStatus(privateChat.getFriendId());
                privateChat.setOnline(onlineStatus.equals(1));

                if (privateChat.getLastMessage() == null || privateChat.getLastMessageDate() == null) {
                    privateChat.setLastMessage("");
                    privateChat.setLastMessageTime("");
                } else {
                    //日期时间格式化
                    privateChat.setLastMessageTime(DateFomatter.formatChatSessionDate(privateChat.getLastMessageDate()));
                }

            }
        }
        // 获取群会话


        return privateChatList;
    }

    @Override
    public ChatSessionInfo getChatInfo(Long talkId, String uid) {
        Integer chatType = getChatType(talkId);
        if (chatType == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "获取会话类型失败,会话不存在");
        }
        String avatar = userMapper.selectSenderInfo(uid).getAvatar();
        if (chatType.equals(GlobalConst.ChatType.PRIVATE_CHAT)) {// 是私聊
            ChatSessionInfo chatSessionInfo = chatMapper.selectPrivateChatData(talkId, uid);
            if (chatSessionInfo == null) {
                throw new BusinessException(ExceptionType.TALK_NOT_VALID, "该会话所对应的聊天不存在或已被删除");
            }
            chatSessionInfo.setAvatar(avatar);
            chatSessionInfo.setIsGroup(false);
            chatSessionInfo.setIsGroupOwner(false);

            return chatSessionInfo;
        } else if (chatType.equals(GlobalConst.ChatType.GROUP_CHAT)) {// 是群聊
            ChatGroup chatGroup = chatMapper.selectChatGroupInfoByChatId(talkId.toString());
            if (chatGroup == null) {
                throw new BusinessException(ExceptionType.TALK_NOT_VALID, "该会话所对应的聊天不存在或已被删除");
            }
            ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
            chatSessionInfo.setAvatar(avatar);
            chatSessionInfo.setTalkId(talkId);
            chatSessionInfo.setIsGroup(true);
            chatSessionInfo.setIsGroupOwner(uid.equals(chatGroup.getOwnerId()));
            chatSessionInfo.setTitle(chatGroup.getGroupName());
            return chatSessionInfo;
        } else {

        }
        return null;

    }

    @Override
    public Boolean setTalkAllMsgHasRead(String uid, String talkId) {
        Integer chatType = getChatType(talkId);
        if (chatType == null) {
            throw new BusinessException(ExceptionType.TALK_NOT_VALID, "获取会话类型失败,会话不存在");
        }
        // 会话未读消息数清零
        setUserChatNewMsgCount(uid,Long.valueOf(talkId),0);

        if (chatType.equals(GlobalConst.ChatType.PRIVATE_CHAT)) {
            return chatMapper.setAllPrivateChatMsgHasRead(Long.valueOf(talkId), uid) > 0;
        }
        return true;
    }

    @Override
    public List<MsgRecord> getMessageRecord(String uid, String talkId, Date beginTime, Integer index, Integer pageSize) {
        if (beginTime == null) {
            beginTime = new Date();
        }

        PageBean pageBean = new PageBean(index, pageSize);
        List<MsgRecord> msgRecordList =
                chatMapper.selectPrivateChatHistoryMsg(Long.valueOf(talkId), beginTime, uid, pageBean);
        if (msgRecordList == null) {
            msgRecordList = new ArrayList<>();
            return msgRecordList;
        }
        Long preMsgTime = null;
        for (int i = msgRecordList.size() - 1; i >= 0; i--) {
            MsgRecord msgRecord = msgRecordList.get(i);
            // 是否显示
            msgRecord.setShowMessage(true);

            // 是否是自己发的
            msgRecord.setUserType(
                    uid.equals(msgRecord.getUserInfo().getUid()) ? 1 : 0
            );

            // 消息类型
            switch (msgRecord.getMessageType()) {
                case 1: {// 普通文本
                    msgRecord.setFileInfo(null);
                    msgRecord.setMessageImgUrl(null);
                    break;
                }
                case 2: {// 图片
                    msgRecord.setFileInfo(null);
                    msgRecord.setMessageText("[图片]");
                    break;
                }
                case 3: {// 文件
                    msgRecord.setMessageImgUrl(null);
                    break;
                }
            }

            // 发送的时间处理
            // 首条或者相差五分钟的才显示时间
            Long thisMsgTime = msgRecord.getMsgTimeDate().getTime();
            boolean showMsgTime = preMsgTime == null || (thisMsgTime - preMsgTime >= GlobalConst.MAX_NOT_SHOW_TIME_SPACE);
            msgRecord.setShowMessageTime(showMsgTime);
            if (showMsgTime) {
                // 时间格式化处理
                String format = DateFomatter.formatMessageDate(msgRecord.getMsgTimeDate());
                msgRecord.setMessageTime(format);
            }
            preMsgTime = msgRecord.getMsgTimeDate().getTime();

            // 群昵称

        }
        Collections.reverse(msgRecordList);
        return msgRecordList;
    }

    @Override
    public Integer getAllHistoryMessageSize(String talkId, String uid, Date beginTime) {
        Integer totalSize = chatMapper.countAllHistoryMsg(Long.valueOf(talkId), beginTime);
        return totalSize == null ? 0 : totalSize;
    }

    @Override
    public Boolean savePrivateMsgRecord(SendMsgDTO msg) {
        PrivateMsgRecord privateMsgRecord = new PrivateMsgRecord();
        // TODO 消息类型的不同应有不同的保存方式
        if (msg.getMessageType().equals(2)){
            privateMsgRecord.setResourceUrl(msg.getMessageImgUrl());
        }else if(msg.getMessageType().equals(3)){
            privateMsgRecord.setResourceUrl(msg.getFileInfo().getDownloadUrl());
        }else{
            privateMsgRecord.setResourceUrl("");
        }

        privateMsgRecord.setMsgId(msg.getMsgId());
        privateMsgRecord.setChatId(Long.valueOf(msg.getTalkId()));
        privateMsgRecord.setContent(msg.getMessageText());
        privateMsgRecord.setFromUid(msg.getSrcId());
        privateMsgRecord.setToUid(msg.getDestId());
        privateMsgRecord.setHasRead(false);
        privateMsgRecord.setMsgType(msg.getMessageType());
        privateMsgRecord.setStatus(1);
        Date msgTime = new Date(Long.parseLong(msg.getTimestamp()));
        privateMsgRecord.setCreatedTime(msgTime);
        privateMsgRecord.setUpdateTime(msgTime);

        return chatMapper.insertPrivateMsgToRecord(privateMsgRecord) > 0;
    }

    @Override
    public Boolean updateChatLastMsg(Long chatId, Long msgId, String senderId) {
        return chatMapper.updatePrivateChatLastMsg(chatId,msgId,senderId) > 0;

    }


    private String[] getUidAUidB(String uid, String friendId) {
        String uidA = uid.compareTo(friendId) < 0 ? uid : friendId;
        String uidB = uid.compareTo(friendId) < 0 ? friendId : uid;
        return new String[]{uidA, uidB};
    }

}
