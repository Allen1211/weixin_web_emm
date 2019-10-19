package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.PageBean;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.DateFomatter;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.dao.ChatDao;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.model.dto.ChatNewMsgSizeDTO;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.pojo.ChatGroup;
import com.allen.imsystem.model.pojo.PrivateChat;
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
    private ChatMapper chatMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String getChatType(String talkIdStr) {
        return redisUtil.hget(GlobalConst.Redis.KEY_CHAT_TYPE,talkIdStr);
    }

    @Override
    public String getChatType(Long talkId) {
        return getChatType(String.valueOf(talkId));
    }

    @Transactional
    public PrivateChat initNewPrivateChat(String uid,String friendId,Boolean status){
        Long chatId = SnowFlakeUtil.getNextSnowFlakeId();
        String uidA = getUidAUidB(uid,friendId)[0];
        String uidB = getUidAUidB(uid,friendId)[1];
        PrivateChat privateChat = new PrivateChat();
        privateChat.setChatId(chatId);
        privateChat.setUidA(uidA);
        privateChat.setUidB(uidB);
        if(uidA.equals(uid)){
            privateChat.setUserAStatus(status);
        }else{
            privateChat.setUserBStatus(status);
        }
        privateChat.setCreatedTime(new Date());
        privateChat.setUpdateTime(new Date());
        chatMapper.insertNewPrivateChat(privateChat);
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,uid+chatId,status?"0":"1");
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,friendId+chatId,"1");
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_TYPE,chatId.toString(),GlobalConst.ChatType.PRIVATE_CHAT);
        return privateChat;
    }


    @Override
    @Transactional
    public Map<String, java.lang.Object> openNewPrivateChat(String uid, String friendId) {
        String uidA = getUidAUidB(uid,friendId)[0];
        String uidB = getUidAUidB(uid,friendId)[1];

        // 获取会话信息
        PrivateChat privateChat = chatMapper.selectPrivateChatInfoByUid(uidA,uidB);

        //判断对方是否已经把自己删掉
        Boolean isDeletedByFriend = friendService.checkIsDeletedByFriend(uid,friendId);
//        if(isDeletedByFriend){  // 对方已经把自己删除，开启新对话应提示
//            throw new BusinessException(ExceptionType.CANNOT_OPEN_PRIVATE_CHAT);
//        }
        if(isDeletedByFriend){   // 被删了
            if(privateChat != null){    // 有会话信息，可以跳转
                return doOpenPrivateChat(uid,friendId,privateChat);
            }else { // 无会话信息，异常
                throw new BusinessException(ExceptionType.TALK_NOT_VALID,"被好友删除，但会话信息也不存在，数据异常");
            }
        }else if(privateChat == null){  // 没被删，而且没有会话信息，为这对好友新建一个会话
            Map<String, Object> result = new HashMap<>(2);
            result.put("privateChat",initNewPrivateChat(uid,friendId,true));
            result.put("isNewTalk",true);
            return result;
        }

        // 没被删，有会话信息，可以跳转，更新会话对当前用户有效
        return doOpenPrivateChat(uid,friendId,privateChat);
    }

    private Map<String, Object> doOpenPrivateChat(String uid,String friendId,PrivateChat privateChat){
        String uidA = getUidAUidB(uid,friendId)[0];
        String uidB = getUidAUidB(uid,friendId)[1];

        boolean isNewTalk;
        if(uidA.equals(uid)){
            isNewTalk = ! privateChat.getUserAStatus();
            privateChat.setUserAStatus(true);
        }else{
            isNewTalk = ! privateChat.getUserBStatus();
            privateChat.setUserBStatus(true);
        }

        // 更新数据库
        chatMapper.updatePrivateChat(privateChat);
        // 设置该用户对该会话的移除为否
        String userChatRemoveKey = uid + privateChat.getChatId();
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,userChatRemoveKey,"0");
        // 绘画类型
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_TYPE,privateChat.getChatId().toString(),GlobalConst.ChatType.PRIVATE_CHAT);

        Map<String, Object> result = new HashMap<>(2);
        result.put("privateChat",privateChat);
        result.put("isNewTalk",isNewTalk);
        return result;
    }

    @Override
    @Transactional
    public void removePrivateChat(String uid, Long chatId) {
        // 设置该用户对该会话的移除为是
        String userChatRemoveKey = uid + chatId;
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,userChatRemoveKey,"1");
        PrivateChat privateChat = chatMapper.selectPrivateChatInfoByChatId(chatId.toString());
        //
        if(uid.equals(privateChat.getUidA())){
            privateChat.setUserAStatus(false);
        }else{
            privateChat.setUserBStatus(false);
        }
        chatMapper.updatePrivateChat(privateChat);
    }

    @Override
    public void removePrivateChat(String uid, String friendId) {
        String uidA = getUidAUidB(uid,friendId)[0];
        String uidB = getUidAUidB(uid,friendId)[1];

        PrivateChat privateChat = chatMapper.selectPrivateChatInfoByUid(uidA,uidB);
        if(uid.equals(privateChat.getUidA())){
            privateChat.setUserAStatus(false);
        }else{
            privateChat.setUserBStatus(false);
        }
        chatMapper.updatePrivateChat(privateChat);
        // 设置该用户对该会话的移除为是
        String userChatRemoveKey = uid + privateChat.getChatId();
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,userChatRemoveKey,"1");
    }

    @Override
    public List<ChatSessionDTO> getChatList(String uid) {
        // 获取私聊会话
        List<ChatSessionDTO> privateChatList = chatDao.selectPrivateChatList(uid);
        Map<Long, ChatNewMsgSizeDTO> sizeMap = chatDao.selectPrivateChatNewMsgSize(privateChatList,uid);
        if(privateChatList != null){
            for(int i=0;i<privateChatList.size();i++){
                ChatSessionDTO privateChat = privateChatList.get(i);

                // 填充新消息条数
                ChatNewMsgSizeDTO sizeDTO = sizeMap.get(privateChat.getTalkId());
                privateChat.setNewMessageCount(sizeDTO == null ? 0:sizeDTO.getSize());

                // 填充在线信息
                String onlineStatus = userService.getUserOnlineStatus(privateChat.getFriendId());
                privateChat.setOnline(onlineStatus.equals("1"));

                if(privateChat.getLastMessage() == null || privateChat.getLastMessageDate()==null){
                    privateChat.setLastMessage("");
                    privateChat.setLastMessageTime("");
                }else{
                    //日期时间格式化
                    privateChat.setLastMessageTime(DateFomatter.formatChatSessionDate(privateChat.getLastMessageDate()));
                }

            }
        }
        // 获取群会话

        return privateChatList;
    }

    @Override
    public ChatSessionInfo getChatInfo(String talkIdStr,String uid) {
        String chatType = getChatType(talkIdStr);
        if(chatType == null){
            throw new BusinessException(ExceptionType.TALK_NOT_VALID,"获取会话类型失败,会话不存在");
        }

        ChatSessionInfo chatSessionInfo = new ChatSessionInfo();
        if(chatType.equals(GlobalConst.ChatType.PRIVATE_CHAT)){// 是私聊
            String title = chatDao.selectPrivateChatTitleName(Long.valueOf(talkIdStr),uid);
            if(title==null){
                throw new BusinessException(ExceptionType.TALK_NOT_VALID,"该会话所对应的聊天不存在或已被删除");
            }
            chatSessionInfo.setTalkId(Long.valueOf(talkIdStr));
            chatSessionInfo.setTitle(title);
            chatSessionInfo.setIsGroup(false);
            chatSessionInfo.setIsGroupOwner(false);
        }else if(chatType.equals(GlobalConst.ChatType.GROUP_CHAT)){// 是群聊
            ChatGroup chatGroup = chatDao.selectChatGroupInfoByChatId(talkIdStr);
            if(chatGroup==null){
                throw new BusinessException(ExceptionType.TALK_NOT_VALID,"该会话所对应的聊天不存在或已被删除");
            }
            chatSessionInfo.setTalkId(Long.valueOf(talkIdStr));
            chatSessionInfo.setIsGroup(true);
            chatSessionInfo.setIsGroupOwner(uid.equals(chatGroup.getOwnerId()));
            chatSessionInfo.setTitle(chatGroup.getGroupName());
        }

        return chatSessionInfo;
    }

    @Override
    public Boolean setTalkAllMsgHasRead(String uid, String talkId) {
        String chatType = getChatType(talkId);
        if(chatType == null){
            throw new BusinessException(ExceptionType.TALK_NOT_VALID,"获取会话类型失败,会话不存在");
        }
        if(chatType.equals(GlobalConst.ChatType.PRIVATE_CHAT)){
            return chatMapper.setAllPrivateChatMsgHasRead(Long.valueOf(talkId),uid)>0;
        }
        return true;
    }

    @Override
    public List<MsgRecord> getMessageRecord(String uid, String talkId,Date beginTime, Integer index, Integer pageSize) {
        if(beginTime == null){
            beginTime = new Date();
        }

        PageBean pageBean = new PageBean(index,pageSize);
//        Date now = new Date();  // 只获取现在这个时间之前的
        List<MsgRecord> msgRecordList =
                chatMapper.selectPrivateChatHistoryMsg(Long.valueOf(talkId), beginTime,uid,pageBean);
        if(msgRecordList == null){
            msgRecordList = new ArrayList<>();
            return  msgRecordList;
        }
        Long preMsgTime = null;
        for(int i=msgRecordList.size()-1;i>=0;i--){
            MsgRecord msgRecord = msgRecordList.get(i);
            // 是否显示
            msgRecord.setShowMsg(true);

            // 是否是自己发的
            if(uid.equals(msgRecord.getUserInfo().getUid())){
                msgRecord.setUserType(1);
            }else{
                msgRecord.setUserType(0);
            }

            // 消息类型
            switch (msgRecord.getMsgType()){
                case 1:{// 普通文本
                    msgRecord.setFileInfo(null);
                    msgRecord.setMessageImgUrl(null);
                    break;
                }
                case 2:{// 图片
                    msgRecord.setFileInfo(null);
                    msgRecord.setMessageText("[图片]");
                    break;
                }
                case 3:{// 文件
                    msgRecord.setMessageImgUrl(null);
                    break;
                }
            }

            // 发送的时间处理
            // 首条或者相差五分钟的才显示时间
            Long thisMsgTime = msgRecord.getMsgTimeDate().getTime();
            boolean showMsgTime = preMsgTime==null || (thisMsgTime-preMsgTime >= 1000*60*5);
            msgRecord.setShowMsgTime(showMsgTime);
            if(showMsgTime){
                String format = DateFomatter.formatMessageDate(msgRecord.getMsgTimeDate());
                msgRecord.setMsgTime(format);
            }
            preMsgTime = msgRecord.getMsgTimeDate().getTime();

            // 群昵称
        }
        Collections.reverse(msgRecordList);
        return msgRecordList;
    }

    @Override
    public Integer getAllHistoryMessageSize(String talkId, String uid,Date beginTime) {
        Integer totalSize = chatMapper.countAllHistoryMsg(Long.valueOf(talkId),beginTime);
        return totalSize==null? 0 : totalSize;
    }

    private String[] getUidAUidB(String uid,String friendId){
        String uidA = uid.compareTo(friendId)<0? uid:friendId;
        String uidB = uid.compareTo(friendId)<0? friendId:uid;
        return new String[]{uidA,uidB};
    }

}
