package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.PageBean;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.dao.ChatDao;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.model.dto.ChatNewMsgSizeDTO;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.ChatSessionInfo;
import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.pojo.ChatGroup;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ChatService implements IChatService {

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private IUserService userService;

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

    @Override
    public void openNewPrivateChat(String uid, String friendId) {
        String uidA = uid.compareTo(friendId)<0? uid:friendId;
        String uidB = uid.compareTo(friendId)<0? friendId:uid;
        // 获取会话信息
        PrivateChat privateChat = chatMapper.selectPrivateChatInfoByUid(uidA,uidB);
        // 设置该用户对该会话的移除为否
        String userChatRemoveKey = uid + privateChat.getChatId();
        redisUtil.hset(GlobalConst.Redis.KEY_CHAT_REMOVE,userChatRemoveKey,"0");
        //
        if(uidA.equals(uid)){
            privateChat.setUserAStatus(true);
        }else{
            privateChat.setUserBStatus(true);
        }
        chatMapper.updatePrivateChat(privateChat);
    }

    @Override
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
    public List<MsgRecord> getMessageRecord(String uid, String talkId, Integer index, Integer pageSize) {
        PageBean pageBean = new PageBean(index,pageSize);
//        Date now = new Date();  // 只获取现在这个时间之前的
        List<MsgRecord> msgRecordList = chatMapper.selectPrivateChatHistoryMsg(Long.valueOf(talkId), uid,pageBean);
        Long preMsgTime = null;
        for(MsgRecord msgRecord : msgRecordList){
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
            // 首条或者相差十分钟的才显示时间
//            Long now = System.currentTimeMillis();
            Long thisMsgTime = msgRecord.getMsgTimeDate().getTime();
            boolean showMsgTime = preMsgTime==null || (thisMsgTime-preMsgTime >= 1000*60*10);
            msgRecord.setShowMsgTime(showMsgTime);
            if(showMsgTime){
//                Date now = new Date();
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(now);
//                // 一天前
//                if(now.getTime() - thisMsgTime > 1000*60*60*24){
//                    // 大于零点
//                    int date = calendar.get(Calendar.DATE);
//
//                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
                msgRecord.setMsgTime(simpleDateFormat.format(thisMsgTime));
            }
            preMsgTime = msgRecord.getMsgTimeDate().getTime();

            // 群昵称
        }
        return msgRecordList;
    }


}
