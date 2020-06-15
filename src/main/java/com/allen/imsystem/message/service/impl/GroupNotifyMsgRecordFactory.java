package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 为群通知类型的消息组装msgRecord
 */
@Component
public class GroupNotifyMsgRecordFactory extends MsgRecordFactory {

    @Override
    public MsgRecord packMsgRecord(SendMsgDTO sendMsgDTO) {
        MsgRecord msgRecord = new MsgRecord();
        msgRecord.setMessageType(4);
        msgRecord.setShowMessage(true);
        msgRecord.setMessageTime(FormatUtil.formatMessageDate(new Date()));
        msgRecord.setMessageText(sendMsgDTO.getMessageText());
        msgRecord.setMessageId(sendMsgDTO.getMsgId());
        msgRecord.setUserType(0);
        return msgRecord;
    }
}