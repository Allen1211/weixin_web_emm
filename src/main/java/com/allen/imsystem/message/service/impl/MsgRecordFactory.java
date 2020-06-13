package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.message.model.vo.MsgRecord;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import org.springframework.stereotype.Component;

@Component
public abstract class MsgRecordFactory {
    public abstract MsgRecord packMsgRecord(SendMsgDTO sendMsgDTO);
}
