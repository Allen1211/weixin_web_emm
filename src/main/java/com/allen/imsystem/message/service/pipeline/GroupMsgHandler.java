package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.message.model.vo.*;

/**
 * @ClassName GroupMsgHandler
 * @Description 抽象的群消息处理器，仅处理群消息，其子类是群消息的具体处理器
 * @Author XianChuLun
 * @Date 2020/6/15
 * @Version 1.0
 */
public abstract class GroupMsgHandler extends MsgHandler{

    @Override
    public void handleMsg(MsgSendPacket<String, SendMsgDTO> msgSendPacket, MsgPushPacket<String, PushMessageDTO> msgPushPacket) {
        // 强转为一对多的消息类型交给子类处理
        handleMsg((OneToOneMsgSendPacket<String, SendMsgDTO>) msgSendPacket, (OneToManyDiffMsgPushPacket<String,PushMessageDTO>)msgPushPacket);
        if(nextHandler != null){
            nextHandler.handleMsg(msgSendPacket,msgPushPacket);
        }
    }

    /**
     * 子类需要实现的处理方法
     */
    abstract public void handleMsg(OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket, OneToManyDiffMsgPushPacket<String, PushMessageDTO> msgPushPacket);

}
