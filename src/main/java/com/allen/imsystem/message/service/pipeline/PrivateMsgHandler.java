package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.message.model.vo.*;


/**
 * @ClassName PrivateMsgHandler
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public abstract class PrivateMsgHandler extends MsgHandler {

    @Override
    public void handleMsg(MsgSendPacket<String, SendMsgDTO> msgSendPacket, MsgPushPacket<String, PushMessageDTO> msgPushPacket) {
        this.handleMsg((OneToOneMsgSendPacket<String, SendMsgDTO>) msgSendPacket, (OneToOneMsgPushPacket<String, PushMessageDTO>) msgPushPacket);
        if (nextHandler != null) {
            nextHandler.handleMsg(msgSendPacket, msgPushPacket);
        }
    }

    abstract public void handleMsg(OneToOneMsgSendPacket<String, SendMsgDTO> sendMsgDTO, OneToOneMsgPushPacket<String, PushMessageDTO> msgPushPacket);
}
