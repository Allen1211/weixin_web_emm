package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.model.pojo.GroupMsgRecord;
import com.allen.imsystem.chat.service.GroupChatService;
import org.springframework.beans.factory.annotation.Autowired;

public class GroupMsgSaveHandler extends MsgHandler {
    @Autowired
    private GroupChatService groupChatService;

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新该群的最后一条消息
        GroupMsgRecord groupMsgRecord = groupChatService.saveGroupChatMsgRecord(sendMsgDTO);
        groupChatService.updateGroupLastMsg(groupMsgRecord.getGid(), groupMsgRecord.getMsgId(),
                groupMsgRecord.getContent(),groupMsgRecord.getCreatedTime(),
                groupMsgRecord.getSenderId());

        if (nextHandler != null) {
            nextHandler.handleMsg(sendMsgDTO, pushMessageDTO);
        }
    }
}
