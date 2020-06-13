package com.allen.imsystem.message.service.impl;


import com.allen.imsystem.common.utils.BeanUtil;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.id.IdPoolService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class MsgHandlerEntry extends MsgHandler {

    private IdPoolService idPoolService;

    public MsgHandlerEntry() {
        this.idPoolService = BeanUtil.getBean(IdPoolService.class);
    }

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        Long msgId = idPoolService.nextMsgId();
        sendMsgDTO.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());
        pushMessageDTO.setChatId(chatId);

        if(nextHandler != null)
            nextHandler.handleMsg(sendMsgDTO,pushMessageDTO);
    }
}
