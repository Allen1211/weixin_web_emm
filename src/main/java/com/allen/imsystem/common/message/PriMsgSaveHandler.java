package com.allen.imsystem.common.message;

import com.allen.imsystem.common.utils.SnowFlakeUtil;
import com.allen.imsystem.model.dto.PushMessageDTO;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.service.IChatService;
import com.allen.imsystem.service.IMessageService;
import com.allen.imsystem.service.impl.MessageCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("priMsgSaveHandler")
@Scope("prototype")
public class PriMsgSaveHandler extends MsgHandler {

    @Autowired
    private IChatService chatService;

    @Autowired
    private MessageCounter messageCounter;

    @Autowired
    private IMessageService messageService;


    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        Long msgId = SnowFlakeUtil.getNextSnowFlakeId();
        sendMsgDTO.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMsgDTO.getTalkId());
        try {
            PrivateMsgRecord msgRecord = chatService.savePrivateMsgRecord(sendMsgDTO);
            chatService.updateChatLastMsg(chatId, msgId,msgRecord.getContent(), msgRecord.getCreatedTime(), sendMsgDTO.getSrcId());
            chatService.setChatLastMsgTimestamp(chatId, Long.parseLong(sendMsgDTO.getTimeStamp()));
        } catch (Exception e) {
            e.printStackTrace();
            messageService.handleSendFail(sendMsgDTO, "消息入库失败");
            return;
        }
        // 入库成功，递增未读信息数
        messageCounter.incrPrivateChatNewMsgCount(sendMsgDTO.getDestId(), chatId);

        if(nextHandler != null)
            nextHandler.handleMsg(sendMsgDTO,pushMessageDTO);
    }
}
