package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.message.model.vo.MsgPushPacket;
import com.allen.imsystem.message.model.vo.MsgSendPacket;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class ServerAckHandler extends MsgHandler {

    @Autowired
    MessageService messageService;

    @Override
    public void handleMsg(MsgSendPacket<String, SendMsgDTO> msgSendPacket, MsgPushPacket<String, PushMessageDTO> msgPushPacket) {
        if(msgSendPacket.isNeedAck()){
            List<SendMsgDTO> sendMessageList = msgSendPacket.getSendMessageList();
            for (SendMsgDTO sendMsgDTO : sendMessageList) {
                messageService.sendServerAck(sendMsgDTO, sendMsgDTO.getMsgId(), Long.parseLong(sendMsgDTO.getTalkId()));
            }
        }
        if (nextHandler != null) {
            nextHandler.handleMsg(msgSendPacket, msgPushPacket);
        }
    }
}
