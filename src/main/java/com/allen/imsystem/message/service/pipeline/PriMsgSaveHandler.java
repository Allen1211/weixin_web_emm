package com.allen.imsystem.message.service.pipeline;

import com.allen.imsystem.chat.service.PrivateChatService;
import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.message.model.vo.OneToOneMsgPushPacket;
import com.allen.imsystem.message.model.vo.OneToOneMsgSendPacket;
import com.allen.imsystem.message.model.vo.PushMessageDTO;
import com.allen.imsystem.message.model.vo.SendMsgDTO;
import com.allen.imsystem.message.model.pojo.PrivateMsgRecord;
import com.allen.imsystem.chat.service.ChatService;
import com.allen.imsystem.message.service.MessageService;
import com.allen.imsystem.id.IdPoolService;
import com.allen.imsystem.message.service.MsgRecordService;
import com.allen.imsystem.message.service.impl.MessageCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("priMsgSaveHandler")
@Scope("prototype")
public class PriMsgSaveHandler extends PrivateMsgHandler {

    @Autowired
    private ChatService chatService;

    @Autowired
    private MsgRecordService msgRecordService;

    @Autowired
    private MessageCounter messageCounter;

    @Autowired
    private MessageService messageService;

    @Autowired
    private IdPoolService idPoolService;

    @Override
    public void handleMsg(OneToOneMsgSendPacket<String, SendMsgDTO> msgSendPacket, OneToOneMsgPushPacket<String, PushMessageDTO> msgPushPacket) {
        SendMsgDTO sendMessage = msgSendPacket.getSendMessage();
        Long msgId = idPoolService.nextMsgId();
        sendMessage.setMsgId(msgId);
        Long chatId = Long.parseLong(sendMessage.getTalkId());
        try {
            PrivateMsgRecord msgRecord = msgRecordService.savePrivateMsgRecord(sendMessage);
            chatService.updateChatLastMsg(GlobalConst.ChatType.PRIVATE_CHAT, chatId.toString(), msgId,msgRecord.getContent(), msgRecord.getCreatedTime(), sendMessage.getSrcId());
        } catch (Exception e) {
            e.printStackTrace();
            messageService.handleSendFail(sendMessage, "消息入库失败");
            return;
        }
        // 入库成功，递增未读信息数
        messageCounter.incrPrivateChatNewMsgCount(sendMessage.getDestId(), chatId);

    }
}
