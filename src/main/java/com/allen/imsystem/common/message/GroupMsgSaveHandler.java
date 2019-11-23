package com.allen.imsystem.common.message;

import com.allen.imsystem.model.dto.PushMessageDTO;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.service.IGroupChatService;
import org.springframework.beans.factory.annotation.Autowired;

public class GroupMsgSaveHandler extends MsgHandler {
    @Autowired
    private IGroupChatService groupChatService;

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        // 2、消息入库
        // 2.1 插入聊天记录 , 并更新该群的最后一条消息
        groupChatService.saveGroupChatMsgRecord(sendMsgDTO);
        groupChatService.updateGroupLastMsg(sendMsgDTO.getGid(), sendMsgDTO.getMsgId(), sendMsgDTO.getSrcId());

        if (nextHandler != null) {
            nextHandler.handleMsg(sendMsgDTO, pushMessageDTO);
        }
    }
}
