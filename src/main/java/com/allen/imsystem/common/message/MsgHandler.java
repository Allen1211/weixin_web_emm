package com.allen.imsystem.common.message;

import com.allen.imsystem.model.dto.PushMessageDTO;
import com.allen.imsystem.model.dto.SendMsgDTO;

public abstract class MsgHandler {

    MsgHandler nextHandler;

    abstract public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO);

    public MsgHandler getNextHandler() {
        return nextHandler;
    }

    public MsgHandler nextHandler(MsgHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }
}
