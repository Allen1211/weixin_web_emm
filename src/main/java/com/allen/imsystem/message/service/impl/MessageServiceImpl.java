package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.bean.ErrMsg;
import com.allen.imsystem.common.utils.BeanUtil;
import com.allen.imsystem.common.utils.MutableSingletonList;
import com.allen.imsystem.message.model.vo.*;
import com.allen.imsystem.message.netty.bean.MultiDataSocketResponse;
import com.allen.imsystem.message.netty.bean.ServerAckDTO;
import com.allen.imsystem.message.netty.bean.SocketResponse;
import com.allen.imsystem.message.service.pipeline.MsgHandler;
import com.allen.imsystem.message.service.pipeline.MsgHandlerFactory;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.chat.mappers.group.GroupMapper;
import com.allen.imsystem.friend.service.FriendQueryService;
import com.allen.imsystem.message.service.MessageService;
import com.allen.imsystem.chat.model.pojo.Group;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.message.netty.WsEventHandler;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.allen.imsystem.message.service.pipeline.MsgHandlerFactory.*;

@Lazy
@DependsOn("beanUtil")
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private WsEventHandler wsEventHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private FriendQueryService friendQueryService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private MessageCounter messageCounter;

    private MsgHandler privateMsgHandler;

    private MsgHandler groupMsgHandler;

    public MessageServiceImpl() {
    }


    @Override
    public void saveAndForwardPrivateMessage(SendMsgDTO sendMsgDTO) {

        // 0、检查是否被对方删除
        boolean isDeleteByFriend = friendQueryService.checkIsDeletedByFriend(sendMsgDTO.getSrcId(), sendMsgDTO.getDestId());
        if (isDeleteByFriend) {
            handleSendFail(sendMsgDTO, "对方还不是你的好友，无法发送消息");
            return;
        }
        // 通过处理链处理消息
        OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket =
                new OneToOneMsgSendPacket<>(sendMsgDTO.getSrcId(), sendMsgDTO);
        OneToOneMsgPushPacket<String, PushMessageDTO> msgPushPacket =
                new OneToOneMsgPushPacket<>(GlobalConst.WsEvent.SERVER_PUSH_MSG, sendMsgDTO.getDestId(), new PushMessageDTO());
        getMsgHandler(MsgHandlerType.PRIVATE_MSG).handleMsg(msgSendPacket, msgPushPacket);

        if (msgPushPacket.isNeedToPush()) {   // 如果在线，转发消息，TODO 并把消息存入缓存，等待接收者已读回执。
            wsEventHandler.handleServerPush(GlobalConst.WsEvent.SERVER_PUSH_MSG, sendMsgDTO.getDestId(), msgPushPacket.getPushMessage());
        }
    }

    @Override
    public void saveAndForwardGroupMessage(SendMsgDTO sendMsgDTO) {
        String gid = sendMsgDTO.getGid();

        // 0、检查是否是该群成员
        boolean isMember = groupChatService.checkIsGroupMember(sendMsgDTO.getSrcId(), gid);
        if (!isMember) {
            handleSendFail(sendMsgDTO, "您还不是该群成员，或群已解散，无法发送消息");
            return;
        }

        // 通过处理链处理消息
        OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket =
                new OneToOneMsgSendPacket<>(sendMsgDTO.getSrcId(), sendMsgDTO);
        OneToManyDiffMsgPushPacket<String, PushMessageDTO> msgPacket = new OneToManyDiffMsgPushPacket<>();
        getMsgHandler(MsgHandlerType.GROUP_MSG).handleMsg(msgSendPacket, msgPacket);

        // 推送
        if (msgPacket.isNeedToPush()) {   // 如果在线，推送消息，TODO 并把消息存入缓存，等待接收者已读回执。
            wsEventHandler.handleServerPush(GlobalConst.WsEvent.SERVER_PUSH_MSG, msgPacket.getDestIdList(),
                    msgPacket.getPushMessageList());
        }

    }

    @Override
    public void handleSendFail(SendMsgDTO sendMsgDTO, String content) {
        MultiDataSocketResponse socketResponse =
                new MultiDataSocketResponse(GlobalConst.WsEvent.SERVER_MSG_ACK_FAIL, 0,
                        2001, new ErrMsg(content))
                        .putData("timeStamp", sendMsgDTO.getTimeStamp());

        wsEventHandler.handleServerPush(sendMsgDTO.getSrcId(), socketResponse);
    }

    @Override
    public void sendServerAck(SendMsgDTO sendMsgDTO, Long msgId, Long chatId) {
        ServerAckDTO serverAckDTO = new ServerAckDTO(chatId, msgId, sendMsgDTO.getTimeStamp());
        String messageTime = FormatUtil.formatMessageDate(new Date(Long.parseLong(sendMsgDTO.getTimeStamp())));
        String messageText = parseMessageText(sendMsgDTO);
        serverAckDTO.setLastMessage(messageText);
        serverAckDTO.setLastMessageTime(messageTime);
        new Thread(() -> {
            wsEventHandler.handleServerPush(sendMsgDTO.getSrcId(),
                    new SocketResponse(GlobalConst.WsEvent.SERVER_MSG_ACK_SUCCESS, 1, serverAckDTO));
        }).start();
    }

    @Override
    public void sendGroupNotify(String destId, String gid, List<GroupMsgRecord> notifyList) {
        List<String> destIdList = new MutableSingletonList<>(destId);
        for (GroupMsgRecord notify : notifyList) {
            sendGroupNotify(destIdList,gid,notify);
        }
    }

    @Override
    public void sendGroupNotify(Set<Object> destIdSet, String gid, List<GroupMsgRecord> notifyList) {
        for (GroupMsgRecord notify : notifyList) {
            sendGroupNotify(destIdSet,gid,notify);
        }
    }

    @Override
    public void sendGroupNotify(Set<Object> destIdSet, String gid, GroupMsgRecord notify){
        List<String> destIdList = new ArrayList<>(destIdSet.size());
        for (Object obj : destIdSet) {
            destIdList.add((String) obj);
        }
        sendGroupNotify(destIdList,gid,notify);
    }

    @Override
    public void sendGroupNotify(List<String> destIdList, String gid, GroupMsgRecord notify) {
        SendMsgDTO sendMsgDTO = new SendMsgDTO();
        sendMsgDTO.setMsgId(notify.getMsgId());
        sendMsgDTO.setMessageText(notify.getContent());
        sendMsgDTO.setGid(gid);
        sendMsgDTO.setSrcId(gid);
        sendMsgDTO.setIsGroup(true);
        sendMsgDTO.setMessageType(GlobalConst.MsgType.GROUP_NOTIFY);
        sendMsgDTO.setTimeStamp(String.valueOf(System.currentTimeMillis()));

        // 通过处理链处理消息
        OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket =
                new OneToOneMsgSendPacket<>(sendMsgDTO.getSrcId(), sendMsgDTO);
        OneToManyDiffMsgPushPacket<String, PushMessageDTO> msgPushPacket = new OneToManyDiffMsgPushPacket<>(destIdList);

        getMsgHandler(MsgHandlerType.GROUP_NOTIFY).handleMsg(msgSendPacket, msgPushPacket);

        // 推送
        if (msgPushPacket.isNeedToPush()) {   // 如果在线，推送消息，TODO 并把消息存入缓存，等待接收者已读回执。
            wsEventHandler.handleServerPush(GlobalConst.WsEvent.SERVER_PUSH_MSG, msgPushPacket.getDestIdList(),
                    msgPushPacket.getPushMessageList());
        }
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

    private MsgHandler getMsgHandler(int type){
        return MsgHandlerFactory.getInstance(type);
    }



}
