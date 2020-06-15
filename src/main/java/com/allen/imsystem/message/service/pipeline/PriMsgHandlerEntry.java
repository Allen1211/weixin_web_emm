package com.allen.imsystem.message.service.pipeline;


import com.allen.imsystem.common.utils.BeanUtil;
import com.allen.imsystem.message.model.vo.OneToOneMsgPushPacket;
import com.allen.imsystem.message.model.vo.OneToOneMsgSendPacket;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.id.IdPoolService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PriMsgHandlerEntry extends PrivateMsgHandler {

    private final IdPoolService idPoolService;

    public PriMsgHandlerEntry() {
        this.idPoolService = BeanUtil.getBean(IdPoolService.class);
    }


    @Override
    public void handleMsg(OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket, OneToOneMsgPushPacket<String, PushMessageDTO> msgPushPacket) {
        SendMsgDTO sendMessage = msgSendPacket.getSendMessage();
        Long msgId = idPoolService.nextMsgId();
        sendMessage.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMessage.getTalkId());

        msgPushPacket.setDestId(sendMessage.getDestId());
        PushMessageDTO pushMessageDTO = msgPushPacket.getPushMessage();
        pushMessageDTO.setChatId(chatId);

    }
}
