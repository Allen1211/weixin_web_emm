package com.allen.imsystem.message.service.impl;

import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.message.service.MessageService;
import com.allen.imsystem.id.IdPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("priMsgSaveHandler")
@Scope("prototype")
public class PriMsgSaveHandler extends MsgHandler {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageCounter messageCounter;

    @Autowired
    private MessageService messageService;

    @Autowired
    private IdPoolService idPoolService;


    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        Long msgId = idPoolService.nextMsgId();
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
