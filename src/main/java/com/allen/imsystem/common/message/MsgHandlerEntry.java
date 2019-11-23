package com.allen.imsystem.common.message;


import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.model.dto.PushMessageDTO;
import com.allen.imsystem.model.dto.SendMsgDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class MsgHandlerEntry extends MsgHandler {

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        Long msgId = SnowFlakeUtil.getNextSnowFlakeId();
        sendMsgDTO.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());
        pushMessageDTO.setChatId(chatId);

        if(nextHandler != null)
            nextHandler.handleMsg(sendMsgDTO,pushMessageDTO);
    }
}
