package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.message.model.vo.OneToManyDiffMsgPushPacket;
import com.allen.imsystem.message.model.vo.OneToOneMsgSendPacket;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.chat.service.GroupChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("prototype")
public class GroupMsgSaveHandler extends GroupMsgHandler {

    @Autowired
    private GroupChatService groupChatService;

    @Override
    @Transactional
    public void handleMsg(OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket, OneToManyDiffMsgPushPacket<String, PushMessageDTO> msgPacket) {
        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新该群的最后一条消息
        GroupMsgRecord groupMsgRecord = groupChatService.saveGroupChatMsgRecord(msgSendPacket.getSendMessage());
        groupChatService.updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(),
                groupMsgRecord.getContent(),groupMsgRecord.getCreatedTime(),
                groupMsgRecord.getSenderId());

    }
}
