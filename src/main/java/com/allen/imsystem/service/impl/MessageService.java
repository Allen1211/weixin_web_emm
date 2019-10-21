package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.DateFomatter;
import com.allen.imsystem.common.utils.RedisUtil;
import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.controller.websocket.WebSocketEventHandler;
import com.allen.imsystem.dao.mappers.ChatMapper;
import com.allen.imsystem.dao.mappers.UserMapper;
import com.allen.imsystem.model.dto.*;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IFriendService;
import com.allen.imsystem.service.IMessageService;
import com.allen.imsystem.service.IUserService;
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
            webSocketEventHandler.handleResponse(sendMsgDTO.getSrcId(),new SocketResponse(203, 0,
                    2001, new ErrMsg("对方还不是您的好友，或您已被对方删除"), null));
            return;
        }
        // 1、为该条信息生成id
        Long msgId = SnowFlakeUtil.getNextSnowFlakeId();
        sendMsgDTO.setMsgId(msgId);

        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新会话的最后一条消息
        boolean insertSuccess = chatService.savePrivateMsgRecord(sendMsgDTO);
        chatService.updateChatLastMsg(Long.valueOf(sendMsgDTO.getTalkId()), msgId, sendMsgDTO.getSrcId());
        chatService.incrUserChatNewMsgCount(sendMsgDTO.getDestId(),Long.valueOf(sendMsgDTO.getTalkId()));
        // 3、入库成功，发送服务端收到确认回执
        if (insertSuccess) {
            ServerAckDTO serverAckDTO = new ServerAckDTO(msgId, sendMsgDTO.getTimestamp());
            new Thread(() -> {
                webSocketEventHandler.handleResponse(sendMsgDTO.getSrcId(),
                        new SocketResponse(202,1,serverAckDTO));
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
        ChatSessionDTO talkData = null;
        if (sendMsgDTO.getIsGroup()) {    // 若是群聊

        } else {  // 若不是群聊
            talkData = chatMapper.selectNewMsgPrivateChatData(talkId,sendMsgDTO.getDestId(),sendMsgDTO.getSrcId());
            talkData.setLastMessageTime(DateFomatter.formatChatSessionDate(talkData.getLastMessageDate()));
            talkData.setNewMessageCount(chatService.getUserChatNewMsgCount(sendMsgDTO.getDestId(),talkId));
        }
        result.setTalkData(talkData);

        // 3、填充消息体
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageId(sendMsgDTO.getMsgId());
        msgRecord.setUserType(0);
        msgRecord.setMsgType(sendMsgDTO.getMessageType());
        msgRecord.setMessageText(sendMsgDTO.getMessageText());
        // TODO 图片信息，应该返回URL
        msgRecord.setMessageImgUrl(sendMsgDTO.getMessageImgData());
        // TODO 文件信息，应该返回文件下载URL
        msgRecord.setFileInfo(null);

        // 发送者信息
        UserInfoDTO userInfo = userMapper.selectSenderInfo(sendMsgDTO.getSrcId());
        msgRecord.setUserInfo(userInfo);
        // 消息时间
        Date msgTimeDate = new Date(Long.valueOf(sendMsgDTO.getTimestamp()));
        String msgTimeStr = DateFomatter.formatMessageDate(msgTimeDate);
        msgRecord.setMsgTimeDate(msgTimeDate);
        msgRecord.setMsgTime(msgTimeStr);
        // 是否显示时间
        Long chatLastMsgTime = chatService.getChatLastMsgTimestamp(talkId);
        Long thisMsgTime = Long.valueOf(sendMsgDTO.getTimestamp());
        boolean showTime = thisMsgTime - chatLastMsgTime > GlobalConst.MAX_NOT_SHOW_TIME_SPACE;
        msgRecord.setShowMsgTime(showTime);
        // TODO 群昵称

        result.setMessageData(msgRecord);

        return result;
    }
}
