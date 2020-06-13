package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;

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
