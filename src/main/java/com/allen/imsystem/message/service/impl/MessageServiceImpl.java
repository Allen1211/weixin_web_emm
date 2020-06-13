package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.bean.ErrMsg;
import com.allen.imsystem.file.model.MsgFileInfo;
import com.allen.imsystem.id.IdPoolService;
import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.netty.bean.MultiDataSocketResponse;
import com.allen.imsystem.message.netty.bean.ServerAckDTO;
import com.allen.imsystem.message.netty.bean.SocketResponse;
import com.allen.imsystem.user.model.vo.UserInfoView;
import com.allen.imsystem.common.utils.BeanUtil;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.chat.mappers.group.GroupMapper;
import com.allen.imsystem.friend.service.FriendQueryService;
import com.allen.imsystem.user.mappers.UserMapper;
import com.allen.imsystem.file.service.FileService;
import com.allen.imsystem.message.service.MessageService;
import com.allen.imsystem.chat.model.pojo.Group;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.message.netty.WsEventHandler;
import com.allen.imsystem.common.redis.RedisService;
import com.allen.imsystem.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private WsEventHandler wsEventHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FriendQueryService friendQueryService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GroupChatService groupChatService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private MessageCounter messageCounter;

    @Autowired
    private IdPoolService idPoolService;

    @Override
    public void saveAndForwardPrivateMessage(SendMsgDTO sendMsgDTO) {

        // 0、检查是否被对方删除
        boolean isDeleteByFriend = friendQueryService.checkIsDeletedByFriend(sendMsgDTO.getSrcId(), sendMsgDTO.getDestId());
        if (isDeleteByFriend) {
            handleSendFail(sendMsgDTO, "对方还不是你的好友，无法发送消息");
            return;
        }
        // 创建处理链
        MsgHandler handlerEntry = BeanUtil.getBean(MsgHandlerEntry.class);    // 处理链入口
        MsgHandler priMsgSaveHandler = BeanUtil.getBean(PriMsgSaveHandler.class); // 消息入库
        MsgHandler serverAckHandler = BeanUtil.getBean(ServerAckHandler.class);   // 服务端确认回执
        MsgHandler priMsgTalkDataHandler = BeanUtil.getBean(PriMsgTalkDataHandler.class); // 消息回话数据填充
        MsgHandler msgRecordPackHandler =
                BeanUtil.getBean(MsgRecordPackHandler.class, BeanUtil.getBean(NormalMsgRecordFactory.class));   // 消息内容填充
        handlerEntry.nextHandler(priMsgSaveHandler)
                .nextHandler(serverAckHandler)
                .nextHandler(priMsgTalkDataHandler)
                .nextHandler(msgRecordPackHandler);

        PushMessageDTO pushMessageDTO = new PushMessageDTO();
        handlerEntry.handleMsg(sendMsgDTO, pushMessageDTO);
        // 4、查看接收者是否在线
        boolean isOnline = userService.isOnline(sendMsgDTO.getSrcId());
        if (isOnline) {   // 如果在线，转发消息，TODO 并把消息存入缓存，等待接收者已读回执。
            wsEventHandler.handleResponse(GlobalConst.WsEvent.SERVER_PUSH_MSG, sendMsgDTO.getDestId(), pushMessageDTO);
        }
    }

    @Override
    public void saveAndForwardGroupMessage(SendMsgDTO sendMsgDTO) {
        String gid = sendMsgDTO.getGid();
        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());

        // 0、检查是否是该群成员
        boolean isMember = groupChatService.checkIsGroupMember(sendMsgDTO.getSrcId(), gid);
        if (!isMember) {
            handleSendFail(sendMsgDTO, "您还不是该群成员，或群已解散，无法发送消息");
            return;
        }
        // 1、为该条信息生成id
        Long msgId = idPoolService.nextMsgId();
        sendMsgDTO.setMsgId(msgId);

        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新该群的最后一条消息
        GroupMsgRecord groupMsgRecord = groupChatService.saveGroupChatMsgRecord(sendMsgDTO);
        // 更新会话最后一条信息
        groupChatService.updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(), groupMsgRecord.getContent(),
                groupMsgRecord.getCreatedTime(), groupMsgRecord.getSenderId());
        // 3、入库成功，发送服务端收到确认回执
        sendServerAck(sendMsgDTO, msgId, chatId);

        // 组装msgRecord bean
        Long chatLastMsgTime = chatService.getChatLastMsgTimestamp(Long.parseLong(gid));
        MsgRecord msgRecord = packNormalMsgRecord(chatLastMsgTime, sendMsgDTO);

        // 4、 转发给其他群员
        Set<Object> memberIdSet = groupChatService.getGroupMemberFromCache(gid);
        memberIdSet.remove(sendMsgDTO.getSrcId());  //去掉发送者
        // 发送
        sendGroupMessage(GlobalConst.WsEvent.SERVER_PUSH_MSG, memberIdSet, gid, msgRecord);

    }

    public void handleSendFail(SendMsgDTO sendMsgDTO, String content) {
        MultiDataSocketResponse socketResponse =
                new MultiDataSocketResponse(GlobalConst.WsEvent.SERVER_MSG_ACK_FAIL, 0,
                        2001, new ErrMsg(content))
                        .putData("timeStamp", sendMsgDTO.getTimeStamp());

        wsEventHandler.handleResponse(sendMsgDTO.getSrcId(), socketResponse);
    }

    public void sendServerAck(SendMsgDTO sendMsgDTO, Long msgId, Long chatId) {
        ServerAckDTO serverAckDTO = new ServerAckDTO(chatId, msgId, sendMsgDTO.getTimeStamp());
        String messageTime = FormatUtil.formatMessageDate(new Date(Long.parseLong(sendMsgDTO.getTimeStamp())));
        String messageText = parseMessageText(sendMsgDTO);
        serverAckDTO.setLastMessage(messageText);
        serverAckDTO.setLastMessageTime(messageTime);
        new Thread(() -> {
            wsEventHandler.handleResponse(sendMsgDTO.getSrcId(),
                    new SocketResponse(GlobalConst.WsEvent.SERVER_MSG_ACK_SUCCESS, 1, serverAckDTO));
        }).start();
    }

    public void sendGroupNotify(String destId, String gid, List<GroupMsgRecord> notifyList) {
        Set<Object> destIdSet = new HashSet<>(1);
        destIdSet.add(destId);
        sendGroupNotify(destIdSet, gid, notifyList);
    }

    @Override
    public void sendGroupNotify(Set<Object> destIdList, String gid, List<GroupMsgRecord> notifyList) {
        List<MsgRecord> msgRecordList = new ArrayList<>(notifyList.size());
        for (GroupMsgRecord msg : notifyList) {
            msgRecordList.add(packNotifyMsgRecord(msg));
        }
        sendGroupMessage(201, destIdList, gid, msgRecordList);
    }

    @Override
    public void sendGroupNotify(Set<Object> destIdList, String gid, GroupMsgRecord notify) {
        this.sendGroupMessage(201, destIdList, gid,
                packNotifyMsgRecord(notify));
    }

    private void sendGroupMessage(Integer eventCode, Set<Object> destIdList, String gid, List<MsgRecord> msgRecordList) {
        // 接收者增加一条未读信息
        messageCounter.incrGroupChatNewMsgCount(destIdList, gid);

        List<String> onlineList = new ArrayList<>(destIdList.size());
        for (Object destIdObj : destIdList) {
            String destId = (String) destIdObj;
            Integer onlineStatus = userService.getUserOnlineStatus(destId);
            if (!GlobalConst.UserStatus.OFFLINE.equals(onlineStatus)) {
                onlineList.add(destId);
            }
        }
        if (!onlineList.isEmpty()) {
            Group group = groupMapper.findByGId(gid);
            Map<String, ChatSession> allChatData = groupChatService.getAllGroupChatSession(gid);
            PushMessageDTO pushMessageDTO = new PushMessageDTO();
            for (MsgRecord msgRecord : msgRecordList) {
                pushMessageDTO.setMessageData(msgRecord);
                for (String destId : onlineList) {
                    // 组装会话信息
                    ChatSession chatSession = allChatData.get(destId);
                    if (chatSession != null) {
                        chatSession.setAvatar(group.getAvatar());
                        chatSession.setLastMessage(group.getLastMsgContent());
                        chatSession.setTalkTitle(group.getGroupName());
                        chatSession.setLastMessageDate(group.getLastMsgCreateTime());

                        boolean isNewTalk = checkIsNewGroupChatAndActivate(destId, gid); //判断是否是新会话，是的话激活为显示状态
                        pushMessageDTO.setIsNewTalk(isNewTalk);
                        pushMessageDTO.setLastTimeStamp(chatSession.getLastMessageDate().getTime());
                        pushMessageDTO.setChatId(chatSession.getChatId());
                        // 未读信息数
                        chatSession.setNewMessageCount(messageCounter.getUserGroupChatNewMsgCount(destId, gid));
                    }
                    pushMessageDTO.setTalkData(chatSession);
                    // 组装完成，发送
                    wsEventHandler.handleResponse(eventCode, destId, pushMessageDTO);
                }
            }
        }
    }

    private void sendGroupMessage(Integer eventCode, Set<Object> destIdList, String gid, MsgRecord msgRecord) {
        List<MsgRecord> msgRecordList = new ArrayList<>(1);
        msgRecordList.add(msgRecord);
        sendGroupMessage(eventCode, destIdList, gid, msgRecordList);
    }


    /**
     * 组装群通知
     *
     * @param
     * @return
     */
    private MsgRecord packNotifyMsgRecord(GroupMsgRecord groupMsg) {
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageType(4);
        msgRecord.setShowMessage(true);
        msgRecord.setMessageTime(FormatUtil.formatMessageDate(new Date()));
        msgRecord.setMessageText(groupMsg.getContent());
        msgRecord.setMessageId(groupMsg.getMsgId());
        msgRecord.setUserType(0);
        return msgRecord;
    }

    /**
     * @param chatLastMsgTime
     * @param sendMsgDTO
     * @return
     */
    private MsgRecord packNormalMsgRecord(Long chatLastMsgTime, SendMsgDTO sendMsgDTO) {
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageId(sendMsgDTO.getMsgId());
        msgRecord.setUserType(0);
        msgRecord.setMessageType(sendMsgDTO.getMessageType());

        switch (msgRecord.getMessageType()) {
            case GlobalConst.MsgType.TEXT: {
                msgRecord.setMessageText(sendMsgDTO.getMessageText());
                break;
            }
            case GlobalConst.MsgType.IMAGE: {
                msgRecord.setMessageText("[图片]");
                msgRecord.setMessageImgUrl(sendMsgDTO.getMessageImgUrl());
                break;
            }
            case GlobalConst.MsgType.FILE: {
                MsgFileInfo fileInfo = sendMsgDTO.getFileInfo();
                String md5 = fileService.getMd5FromUrl(fileInfo.getDownloadUrl());
                String sizeStr = (String) redisService.get(md5);
                Long size = sizeStr == null ? 0L : Long.parseLong(sizeStr);
                String fileSize = FormatUtil.formatFileSize(size);
                fileInfo.setFileSize(fileSize);
                fileInfo.setSize(size);
                msgRecord.setFileInfo(fileInfo);
                // 文件消息内容为文件名
                msgRecord.setMessageText(fileInfo.getFileName());
                break;
            }

        }

        // 4.1发送者信息
        UserInfoView userInfo = userMapper.selectUserInfoDTO(sendMsgDTO.getSrcId());
        msgRecord.setUserInfo(userInfo);
        // 4.2消息时间
        Long msgSendTimestamp = Long.parseLong(sendMsgDTO.getTimeStamp());
        Date msgTimeDate = new Date(msgSendTimestamp);
        String msgTimeStr = FormatUtil.formatMessageDate(msgTimeDate);
        msgRecord.setMsgTimeDate(msgTimeDate);
        msgRecord.setMessageTime(msgTimeStr);
        // 4.3是否显示时间 消息发送时间 - 会话上一条消息发送时间 > 固定时间 即显示时间
        boolean showTime = msgSendTimestamp - chatLastMsgTime > GlobalConst.MAX_NOT_SHOW_TIME_SPACE;
        msgRecord.setShowMessageTime(showTime);

        return msgRecord;
    }

    private boolean checkIsNewGroupChatAndActivate(String uid, String gid) {
        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
        boolean isNewTalk = !chatService.isGroupChatSessionOpenToUser(uid, gid);
        if (isNewTalk) {  // 如果是这个用户是新会话，那么更新为显示状态
            chatService.openGroupChat(uid, gid);
        }
        return isNewTalk;
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
