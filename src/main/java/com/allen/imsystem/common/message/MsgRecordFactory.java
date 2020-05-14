package com.allen.imsystem.common.message;

import com.allen.imsystem.model.dto.MsgRecord;
import com.allen.imsystem.model.dto.SendMsgDTO;
import org.springframework.stereotype.Component;

@Component
public abstract class MsgRecordFactory {
    public abstract MsgRecord packMsgRecord(SendMsgDTO sendMsgDTO);
}
