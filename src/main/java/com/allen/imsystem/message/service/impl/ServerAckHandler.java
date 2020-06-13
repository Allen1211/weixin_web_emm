package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ServerAckHandler extends MsgHandler {

    @Autowired
    MessageService messageService;

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        messageService.sendServerAck(sendMsgDTO,sendMsgDTO.getMsgId(),sendMsgDTO.getMsgId());
        nextHandler.handleMsg(sendMsgDTO,pushMessageDTO);
    }
}
