package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.model.pojo.GroupMsgRecord;
import com.allen.imsystem.netty.WebSocketEventHandler;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.dao.mappers.UserMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MessageService implements IMessageService {

    @Autowired
    private WebSocketEventHandler webSocketEventHandler;

    @Autowired
    private IUserService userService;

    @Autowired
    private IChatService chatService;

    @Autowired
    private IFileService fileService;

    @Autowired
    private IFriendService friendService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IGroupChatService groupChatService;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void saveAndForwardPrivateMessage(SendMsgDTO sendMsgDTO) {

        // 0、检查是否被对方删除
        boolean isDeleteByFriend = friendService.checkIsDeletedByFriend(sendMsgDTO.getSrcId(), sendMsgDTO.getDestId());
        if (isDeleteByFriend) {
            handleSendFail(sendMsgDTO,"对方还不是你的好友，无法发送消息");
            return;
        }
        // 1、为该条信息生成id
        Long msgId = SnowFlakeUtil.getNextSnowFlakeId();
        sendMsgDTO.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());
        Long chatLastMsgTime = chatService.getChatLastMsgTimestamp(Long.parseLong(sendMsgDTO.getTalkId()));
        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新会话的最后一条消息
        boolean bool1 = chatService.savePrivateMsgRecord(sendMsgDTO);
        boolean bool2 = chatService.updateChatLastMsg(chatId, msgId, sendMsgDTO.getSrcId());
        boolean bool3 = chatService.setChatLastMsgTimestamp(chatId, Long.parseLong(sendMsgDTO.getTimeStamp()));
        chatService.incrUserChatNewMsgCount(sendMsgDTO.getDestId(), chatId);
        // 3、入库成功，发送服务端收到确认回执
        if (true) {
            sendServerAck(sendMsgDTO, msgId, chatId);
        }

        // 4、查看接收者是否在线
        boolean isOnline = userService.isOnline(sendMsgDTO.getSrcId());
        if (isOnline) {   // 如果在线，转发消息，并把消息存入缓存，等待接收者已读回执。
            PushMessageDTO pushMessageDTO = packPushMessageDTO(chatLastMsgTime,sendMsgDTO);
            webSocketEventHandler.handleResponse(201, sendMsgDTO.getDestId(), pushMessageDTO);
        }
    }

    @Override
    public void saveAndForwardGroupMessage(SendMsgDTO sendMsgDTO) {
        // 0、检查是否是该群成员
        boolean isMember = groupChatService.checkIsGroupMember(sendMsgDTO.getSrcId(),sendMsgDTO.getGid());
        if(!isMember){
            handleSendFail(sendMsgDTO,"您还不是该群成员，或群已解散，无法发送消息");
            return;
        }
        // 1、为该条信息生成id
        Long msgId = SnowFlakeUtil.getNextSnowFlakeId();
        sendMsgDTO.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());

        Long chatLastMsgTime = chatService.getChatLastMsgTimestamp(Long.parseLong(sendMsgDTO.getGid()));

        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新该群的最后一条消息
        groupChatService.saveGroupChatMsgRecord(sendMsgDTO);
        groupChatService.updateGroupLastMsg(sendMsgDTO.getGid(), msgId, sendMsgDTO.getSrcId());
//        chatService.incrUserChatNewMsgCount(sendMsgDTO.getDestId(), chatId);
        // 3、入库成功，发送服务端收到确认回执
        if (true) {
            sendServerAck(sendMsgDTO, msgId, chatId);
        }
        // 4、 转发给其他群员
        Set<Object> memberIdSet = groupChatService.getGroupMemberFromCache(sendMsgDTO.getGid());
        memberIdSet.remove(sendMsgDTO.getSrcId());  //去掉发送者
        MsgRecord msgRecord = packNormalMsgRecord(chatLastMsgTime,sendMsgDTO);
        sendGroupMessage(201,memberIdSet,sendMsgDTO.getGid(),msgRecord);
    }

    private void handleSendFail(SendMsgDTO sendMsgDTO,String content) {
        MultiDataSocketResponse socketResponse =
                new MultiDataSocketResponse(203, 0,
                        2001, new ErrMsg(content))
                        .putData("timeStamp", sendMsgDTO.getTimeStamp());

        webSocketEventHandler.handleResponse(sendMsgDTO.getSrcId(), socketResponse);
    }



    private void sendServerAck(SendMsgDTO sendMsgDTO, Long msgId, Long chatId) {
        ServerAckDTO serverAckDTO = new ServerAckDTO(chatId, msgId, sendMsgDTO.getTimeStamp());
        String messageTime = FormatUtil.formatMessageDate(new Date(Long.parseLong(sendMsgDTO.getTimeStamp())));
        String messageText = parseMessageText(sendMsgDTO);
        serverAckDTO.setLastMessage(messageText);
        serverAckDTO.setLastMessageTime(messageTime);
        new Thread(() -> {
            webSocketEventHandler.handleResponse(sendMsgDTO.getSrcId(),
                    new SocketResponse(202, 1, serverAckDTO));
        }).start();
    }

    /**
     * 将发送过来的像消息组装成推送信息
     *
     * @param sendMsgDTO
     * @return
     */
    private PushMessageDTO packPushMessageDTO(Long chatLastMsgTime,SendMsgDTO sendMsgDTO) {
        PushMessageDTO result = new PushMessageDTO();

        // 1、会话Id
        Long talkId = Long.valueOf(sendMsgDTO.getTalkId());
        result.setTalkId(talkId);

        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
        Boolean isNewTalk;
        if (sendMsgDTO.getIsGroup()) {

        } else {
            checkIsNewPrivateChatAndActivate(sendMsgDTO.getDestId(), talkId);
        }
        // 3、填充会话信息
        ChatSessionDTO talkData = null;
        if (sendMsgDTO.getIsGroup()) {    // 若是群聊

        } else {  // 若不是群聊
            talkData = chatMapper.selectNewMsgPrivateChatData(talkId, sendMsgDTO.getDestId(), sendMsgDTO.getSrcId());
            talkData.setLastMessageTime(FormatUtil.formatChatSessionDate(talkData.getLastMessageDate()));
            talkData.setNewMessageCount(chatService.getUserChatNewMsgCount(sendMsgDTO.getDestId(), talkId));
        }
        result.setTalkData(talkData);
        result.setLastTimeStamp(talkData.getLastMessageDate().getTime());
        // 4、填充消息体
        MsgRecord msgRecord = packNormalMsgRecord(chatLastMsgTime,sendMsgDTO);
        // TODO 群昵称

        result.setMessageData(msgRecord);

        return result;
    }

    @Override
    public void sendGroupNotify(Set<Object> destIdList, String gid, List<GroupMsgRecord> notifyList) {
        List<MsgRecord> msgRecordList = new ArrayList<>(notifyList.size());
        for (GroupMsgRecord msg : notifyList) {
            msgRecordList.add(packNotifyMsgRecord(msg.getMsgId(), msg.getContent()));
        }
        sendGroupMessage(201,destIdList, gid, msgRecordList);
    }

    @Override
    public void sendGroupNotify(Set<Object> destIdList, String gid, GroupMsgRecord notify) {
        this.sendGroupMessage(201,destIdList,gid,
                packNotifyMsgRecord(notify.getMsgId(), notify.getContent()));
    }

    @Override
    public void sendGroupMessage(Integer eventCode ,Set<Object> destIdList, String gid, List<MsgRecord> msgRecordList) {
        List<String> onlineList = new ArrayList<>(destIdList.size());
        for (Object destIdObj : destIdList) {
            String destId = (String) destIdObj;
            Integer onlineStatus = userService.getUserOnlineStatus(destId);
            if (!GlobalConst.UserStatus.OFFLINE.equals(onlineStatus)) {
                onlineList.add(destId);
            }
        }
        if (!onlineList.isEmpty()) {
            Map<String, ChatSessionDTO> allChatData = groupChatService.getAllGroupChatSession(gid);
            PushMessageDTO pushMessageDTO = new PushMessageDTO();
            for (MsgRecord msgRecord : msgRecordList) {
                pushMessageDTO.setMessageData(msgRecord);
                for (String destId : onlineList) {
                    ChatSessionDTO chatSessionDTO = allChatData.get(destId);
                    if (chatSessionDTO != null) {
                        boolean isNewTalk = !chatService.isChatSessionOpenToUser(destId, chatSessionDTO.getTalkId());
                        pushMessageDTO.setIsNewTalk(isNewTalk);
                        pushMessageDTO.setLastTimeStamp(chatSessionDTO.getLastMessageDate().getTime());
                        pushMessageDTO.setTalkId(chatSessionDTO.getTalkId());
                    }
                    pushMessageDTO.setTalkData(chatSessionDTO);
                    // 组装完成，发送
                    webSocketEventHandler.handleResponse(eventCode, destId, pushMessageDTO);
                }
            }
        }
    }
    @Override
    public void sendGroupMessage(Integer eventCode,Set<Object> destIdList, String gid, MsgRecord msgRecord) {
        List<MsgRecord> msgRecordList = new ArrayList<>(1);
        msgRecordList.add(msgRecord);
        sendGroupMessage(eventCode,destIdList, gid, msgRecordList);
    }

    /**
     * 组装群通知
     *
     * @param
     * @return
     */
    private MsgRecord packNotifyMsgRecord(Long msgId, String content) {
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageType(4);
        msgRecord.setShowMessage(true);
        msgRecord.setMessageTime(FormatUtil.formatMessageDate(new Date()));
        msgRecord.setMessageText(content);
        msgRecord.setMessageId(msgId);
        msgRecord.setUserType(0);
        return msgRecord;
    }

    private MsgRecord packNormalMsgRecord(Long chatLastMsgTime,SendMsgDTO sendMsgDTO){
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageId(sendMsgDTO.getMsgId());
        msgRecord.setUserType(0);
        msgRecord.setMessageType(sendMsgDTO.getMessageType());
        msgRecord.setMessageText(sendMsgDTO.getMessageText());

        switch (msgRecord.getMessageType()) {
            case 1: {
                msgRecord.setMessageText(sendMsgDTO.getMessageText());
                break;
            }
            case 2: {
                msgRecord.setMessageText("[图片]");
                msgRecord.setMessageImgUrl(sendMsgDTO.getMessageImgUrl());
                break;
            }
            case 3: {
                MsgFileInfo fileInfo = sendMsgDTO.getFileInfo();
                String md5 = fileService.getMd5FromUrl(3, fileInfo.getDownloadUrl());
                String sizeStr = (String) redisService.get(md5);
                Long size = 0L;
                if (sizeStr == null) {
                    size = 0L;
                } else {
                    size = Long.parseLong(sizeStr);
                }
                String fileSize = FormatUtil.formatFileSize(size);
                fileInfo.setFileSize(fileSize);
                fileInfo.setSize(size);
                msgRecord.setFileInfo(fileInfo);
                break;
            }

        }

        // 图片信息，应该返回URL
        if (msgRecord.getMessageType().equals(2))
            msgRecord.setMessageImgUrl(sendMsgDTO.getMessageImgUrl());
        // 文件信息，应该返回文件下载URL
        if (msgRecord.getMessageType().equals(3))
            msgRecord.setFileInfo(sendMsgDTO.getFileInfo());

        // 4.1发送者信息
        UserInfoDTO userInfo = userMapper.selectSenderInfo(sendMsgDTO.getSrcId());
        msgRecord.setUserInfo(userInfo);
        // 4.2消息时间
        Date msgTimeDate = new Date(Long.parseLong(sendMsgDTO.getTimeStamp()));
        String msgTimeStr = FormatUtil.formatMessageDate(msgTimeDate);
        msgRecord.setMsgTimeDate(msgTimeDate);
        msgRecord.setMessageTime(msgTimeStr);
        // 4.3是否显示时间
        Long thisMsgTime = Long.valueOf(sendMsgDTO.getTimeStamp());
        boolean showTime = thisMsgTime - chatLastMsgTime > GlobalConst.MAX_NOT_SHOW_TIME_SPACE;
        msgRecord.setShowMessageTime(showTime);

        // TODO 群昵称
        return msgRecord;
    }

    private boolean checkIsNewPrivateChatAndActivate(String uid, Long chatId) {
        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
        boolean isNewTalk = !chatService.isChatSessionOpenToUser(uid, chatId);

        if (isNewTalk) { // 如果是新会话，更新
            redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, uid + chatId, true);
            new Thread(() -> {
                PrivateChat privateChat = new PrivateChat();
                privateChat.setChatId(chatId);
                privateChat.setUserAStatus(true);
                privateChat.setUserBStatus(true);
                privateChat.setUpdateTime(new Date());
                chatMapper.updatePrivateChat(privateChat);
            }).start();
        }
        return isNewTalk;
    }

//    private boolean checkIsNewGroupChatAndActivate(String uid, Long chatId){
//        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
//        boolean isNewTalk = !chatService.isChatSessionOpenToUser(uid, chatId);
//
//        if (isNewTalk) { // 如果是新会话，更新
//            redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, uid+chatId, true);
//            new Thread(() -> {
//                PrivateChat privateChat = new PrivateChat();
//                privateChat.setChatId(chatId);
//                privateChat.setUserAStatus(true);
//                privateChat.setUserBStatus(true);
//                privateChat.setUpdateTime(new Date());
//                chatMapper.updatePrivateChat(privateChat);
//            }).start();
//        }
//        return isNewTalk;
//    }

    private String parseMessageText(SendMsgDTO sendMsgDTO) {
        Integer msgType = sendMsgDTO.getMessageType();
        String msgText = sendMsgDTO.getMessageText();
        if (msgType.equals(1)) {
            msgText = msgText == null ? "" : msgText;
        } else if (msgType.equals(2)) {
            msgText = "[图片]";
        } else if (msgType.equals(3)) {
            msgText = sendMsgDTO.getFileInfo().getFileName();
        }
        sendMsgDTO.setMessageText(msgText);
        return msgText;
    }
}
