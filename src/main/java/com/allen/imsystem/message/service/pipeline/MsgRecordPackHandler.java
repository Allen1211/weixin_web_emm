package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.message.model.vo.*;
import com.allen.imsystem.message.service.impl.MsgRecordFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class MsgRecordPackHandler extends MsgHandler {

    private MsgRecordFactory msgRecordFactory;

    public MsgRecordPackHandler(MsgRecordFactory msgRecordFactory) {
        this.msgRecordFactory = msgRecordFactory;
    }

    @Override
    public void handleMsg(MsgSendPacket<String, SendMsgDTO> msgSendPacket, MsgPushPacket<String, PushMessageDTO> msgPushPacket) {

        List<SendMsgDTO> sendMessageList = msgSendPacket.getSendMessageList();

        SendMsgDTO sendMsgDTO = sendMessageList.get(0);

        MsgRecord msgRecord = msgRecordFactory.packMsgRecord(sendMsgDTO);
        List<PushMessageDTO> pushMessageList = msgPushPacket.getPushMessageList();
        for (PushMessageDTO pushMessage : pushMessageList) {
            pushMessage.setMessageData(msgRecord);
        }

        if (nextHandler != null) {
            nextHandler.handleMsg(msgSendPacket, msgPushPacket);
        }
    }
}
