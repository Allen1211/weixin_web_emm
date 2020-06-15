package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.message.model.vo.MsgPushPacket;
import com.allen.imsystem.message.model.vo.MsgSendPacket;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;

/**
 * 抽象地表示一个消息处理器
 */
public abstract class MsgHandler {

    /**
     * 连接的下一个处理器
     */
    protected MsgHandler nextHandler;

    abstract public void handleMsg(MsgSendPacket<String, SendMsgDTO> msgSendPacket, MsgPushPacket<String, PushMessageDTO> msgPushPacket);

    public MsgHandler getNextHandler() {
        return nextHandler;
    }

    public MsgHandler nextHandler(MsgHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }
}
