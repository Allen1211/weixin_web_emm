package com.allen.imsystem.common.message;

import com.allen.imsystem.model.dto.PushMessageDTO;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ServerAckHandler extends MsgHandler {

    @Autowired
    IMessageService messageService;

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        messageService.sendServerAck(sendMsgDTO,sendMsgDTO.getMsgId(),sendMsgDTO.getMsgId());
    }
}
