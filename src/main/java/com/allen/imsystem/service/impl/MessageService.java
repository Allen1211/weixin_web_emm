package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.netty.WebSocketEventHandler;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.dao.mappers.UserMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

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
    private ChatMapper chatMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void sendPrivateMessage(SendMsgDTO sendMsgDTO) {
        // 0、检查是否被对方删除
        boolean isDeleteByFriend = friendService.checkIsDeletedByFriend(sendMsgDTO.getSrcId(), sendMsgDTO.getDestId());
        if (isDeleteByFriend) {
            MultiDataSocketResponse socketResponse =
                    new MultiDataSocketResponse(203, 0,
                            2001, new ErrMsg("对方还不是您的好友，或您已被对方删除"))
                            .putData("timeStamp", sendMsgDTO.getTimestamp());

            webSocketEventHandler.handleResponse(sendMsgDTO.getSrcId(), socketResponse);
            return;
        }
        // 1、为该条信息生成id
        Long msgId = SnowFlakeUtil.getNextSnowFlakeId();
        sendMsgDTO.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());
        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新会话的最后一条消息
        boolean bool1 = chatService.savePrivateMsgRecord(sendMsgDTO);
        boolean bool2 = chatService.updateChatLastMsg(chatId, msgId, sendMsgDTO.getSrcId());
        boolean bool3 = chatService.setChatLastMsgTimestamp(chatId,Long.parseLong(sendMsgDTO.getTimestamp()));
        chatService.incrUserChatNewMsgCount(sendMsgDTO.getDestId(), chatId);
        // 3、入库成功，发送服务端收到确认回执
        if (true) {
            ServerAckDTO serverAckDTO = new ServerAckDTO(chatId,msgId, sendMsgDTO.getTimestamp());
            String messageTime = FormatUtil.formatMessageDate(new Date(Long.parseLong(sendMsgDTO.getTimestamp())));
            String messageText = parseMessageText(sendMsgDTO);
            serverAckDTO.setLastMessage(messageText);
            serverAckDTO.setLastMessageTime(messageTime);
            new Thread(() -> {
                webSocketEventHandler.handleResponse(sendMsgDTO.getSrcId(),
                        new SocketResponse(202, 1, serverAckDTO));
            }).start();
        }

        // 4、查看接收者是否在线
        boolean isOnline = userService.isOnline(sendMsgDTO.getSrcId());
        if (isOnline) {   // 如果在线，转发消息，并把消息存入缓存，等待接收者已读回执。
            PushMessageDTO pushMessageDTO = packPushMessageDTO(sendMsgDTO);
            webSocketEventHandler.handleResponse(201, sendMsgDTO.getDestId(), pushMessageDTO);
        }
    }

    /**
     * 组装成推送信息
     *
     * @param sendMsgDTO
     * @return
     */
    public PushMessageDTO packPushMessageDTO(SendMsgDTO sendMsgDTO) {
        PushMessageDTO result = new PushMessageDTO();

        // 1、会话Id
        Long talkId = Long.valueOf(sendMsgDTO.getTalkId());
        result.setTalkId(talkId);

        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
        Boolean isNewTalk = !chatService.isChatSessionOpenToUser(sendMsgDTO.getDestId(), talkId);
        result.setIsNewTalk(isNewTalk);

        if (isNewTalk) { // 如果是新会话，更新
            redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, sendMsgDTO.getDestId() + talkId, true);
            new Thread(() -> {
                PrivateChat privateChat = new PrivateChat();
                privateChat.setChatId(Long.valueOf(talkId));
                privateChat.setUserAStatus(true);
                privateChat.setUserBStatus(true);
                privateChat.setUpdateTime(new Date());
                chatMapper.updatePrivateChat(privateChat);
            }).start();
        }
        // 3、填充会话信息
        ChatSessionDTO talkData = null;
        if (sendMsgDTO.getIsGroup()) {    // 若是群聊

        } else {  // 若不是群聊
            talkData = chatMapper.selectNewMsgPrivateChatData(talkId, sendMsgDTO.getDestId(), sendMsgDTO.getSrcId());
            talkData.setLastMessageTime(FormatUtil.formatChatSessionDate(talkData.getLastMessageDate()));
            talkData.setNewMessageCount(chatService.getUserChatNewMsgCount(sendMsgDTO.getDestId(), talkId) + 1);
        }
        result.setTalkData(talkData);

        // 4、填充消息体
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageId(sendMsgDTO.getMsgId());
        msgRecord.setUserType(0);
        msgRecord.setMessageType(sendMsgDTO.getMessageType());
        msgRecord.setMessageText(sendMsgDTO.getMessageText());

        switch (msgRecord.getMessageType()){
            case 1:{
                msgRecord.setMessageText(sendMsgDTO.getMessageText());
                break;
            }
            case 2:{
                msgRecord.setMessageText("[图片]");
                msgRecord.setMessageImgUrl(sendMsgDTO.getMessageImgUrl());
                break;
            }
            case 3:{
                MsgFileInfo fileInfo = sendMsgDTO.getFileInfo();
                String md5 = fileService.getMd5FromUrl(3,fileInfo.getDownloadUrl());
                String sizeStr = (String) redisService.get(md5);
                Long size = 0L;
                if(sizeStr == null){
                    size = 0L;
                }else{
                    size = Long.parseLong(sizeStr);
                }
                String fileSize = FormatUtil.formatFileSize(size);
                fileInfo.setFileSize(fileSize);
                fileInfo.setSize(size);
                msgRecord.setFileInfo(fileInfo);
                break;
            }
        }

        // TODO 图片信息，应该返回URL
        if(msgRecord.getMessageType().equals(2))
            msgRecord.setMessageImgUrl(sendMsgDTO.getMessageImgUrl());
        // TODO 文件信息，应该返回文件下载URL
        if(msgRecord.getMessageType().equals(3))
            msgRecord.setFileInfo(sendMsgDTO.getFileInfo());

        // 4.1发送者信息
        UserInfoDTO userInfo = userMapper.selectSenderInfo(sendMsgDTO.getSrcId());
        msgRecord.setUserInfo(userInfo);
        // 4.2消息时间
        Date msgTimeDate = new Date(Long.valueOf(sendMsgDTO.getTimestamp()));
        String msgTimeStr = FormatUtil.formatMessageDate(msgTimeDate);
        msgRecord.setMsgTimeDate(msgTimeDate);
        msgRecord.setMessageTime(msgTimeStr);
        // 4.3是否显示时间
        Long chatLastMsgTime = chatService.getChatLastMsgTimestamp(talkId);
        Long thisMsgTime = Long.valueOf(sendMsgDTO.getTimestamp());
        boolean showTime = thisMsgTime - chatLastMsgTime > GlobalConst.MAX_NOT_SHOW_TIME_SPACE;
        msgRecord.setShowMessageTime(showTime);
        // TODO 群昵称

        result.setMessageData(msgRecord);

        return result;
    }


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
