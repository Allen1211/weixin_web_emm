package com.allen.imsystem.common.message;

import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.dto.PushMessageDTO;
import com.allen.imsystem.model.dto.SendMsgDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
@Component
@Scope("prototype")
public class MsgRecordPackHandler extends MsgHandler {

    private MsgRecordFactory msgRecordFactory;

    public MsgRecordPackHandler(MsgRecordFactory msgRecordFactory) {
        this.msgRecordFactory = msgRecordFactory;
    }

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        MsgRecord msgRecord = msgRecordFactory.packMsgRecord(sendMsgDTO);
        pushMessageDTO.setMessageData(msgRecord);
        if(nextHandler != null)
            nextHandler.handleMsg(sendMsgDTO,pushMessageDTO);
    }
}
