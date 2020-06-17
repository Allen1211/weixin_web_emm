package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.chat.mappers.group.GroupMapper;
import com.allen.imsystem.chat.model.pojo.Group;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.chat.service.GroupChatService;
import com.allen.imsystem.message.model.vo.OneToManyDiffMsgPushPacket;
import com.allen.imsystem.message.model.vo.OneToOneMsgSendPacket;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.service.impl.MessageCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class GroupMsgTalkDataHandler extends GroupMsgHandler {

    @Autowired
    private GroupChatService groupChatService;
    @Autowired
    private MessageCounter messageCounter;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private ChatService chatService;

    @Override
    public void handleMsg(OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket, OneToManyDiffMsgPushPacket<String, PushMessageDTO> msgPacket) {

        String gid = msgSendPacket.getSendMessage().getGid();

        List<String> destIdList = msgPacket.getDestIdList();
        List<PushMessageDTO> pushMessageList = msgPacket.getPushMessageList();

        // 接收者增加一条未读信息
        messageCounter.incrGroupChatNewMsgCount(destIdList, gid);

        Group group = groupMapper.findByGId(gid);
        Map<String, ChatSession> allChatData = groupChatService.getAllGroupChatSession(gid);
        for (int i = 0; i < destIdList.size(); i++) {
            String destId = destIdList.get(i);
            PushMessageDTO pushMessage = pushMessageList.get(i);
            // 组装会话信息
            ChatSession chatSession = allChatData.get(destId);
            if (chatSession != null) {
                chatSession.setAvatar(group.getAvatar());
                chatSession.setLastMessage(group.getLastMsgContent());
                chatSession.setTalkTitle(group.getGroupName());
                chatSession.setLastMessageDate(group.getLastMsgCreateTime());

                boolean isNewTalk = checkIsNewGroupChatAndActivate(destId, gid); //判断是否是新会话，是的话激活为显示状态
                pushMessage.setIsNewTalk(isNewTalk);
                pushMessage.setLastTimeStamp(chatSession.getLastMessageDate().getTime());
                pushMessage.setChatId(chatSession.getChatId());
                // 未读信息数
                chatSession.setNewMessageCount(messageCounter.getUserGroupChatNewMsgCount(destId, gid));
            }
            pushMessage.setTalkData(chatSession);
        }

    }

    private boolean checkIsNewGroupChatAndActivate(String uid, String gid) {
        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
        boolean isNewTalk = !groupChatService.isOpen(uid, gid);
        if (isNewTalk) {  // 如果是这个用户是新会话，那么更新为显示状态
            groupChatService.open(uid, gid);
        }
        return isNewTalk;
    }

}
