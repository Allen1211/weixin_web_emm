package com.allen.imsystem.common.message;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.FormatUtil;
import com.allen.imsystem.mappers.ChatMapper;
import com.allen.imsystem.model.dto.ChatSessionDTO;
import com.allen.imsystem.model.dto.PushMessageDTO;
import com.allen.imsystem.model.dto.SendMsgDTO;
import com.allen.imsystem.model.pojo.PrivateChat;
import com.allen.imsystem.service.*;
import com.allen.imsystem.service.impl.MessageCounter;
import com.allen.imsystem.service.impl.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
@Component
@Scope("prototype")
public class PriMsgTalkDataHandler extends MsgHandler {

    @Autowired
    private IChatService chatService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private MessageCounter messageCounter;

    @Override
    public void handleMsg(SendMsgDTO sendMsgDTO, PushMessageDTO pushMessageDTO) {
        Long chatId = pushMessageDTO.getChatId();
        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
        Boolean isNewTalk = checkIsNewPrivateChatAndActivate(sendMsgDTO.getDestId(), chatId);
        pushMessageDTO.setIsNewTalk(isNewTalk);

        // 3、填充会话信息
        ChatSessionDTO talkData = null;
        talkData = chatMapper.selectNewMsgPrivateChatData(chatId, sendMsgDTO.getDestId(), sendMsgDTO.getSrcId());
        talkData.setLastMessageTime(FormatUtil.formatChatSessionDate(talkData.getLastMessageDate()));
        talkData.setNewMessageCount(messageCounter.getPrivateChatNewMsgCount(sendMsgDTO.getDestId(), chatId));
        pushMessageDTO.setTalkData(talkData);
        pushMessageDTO.setLastTimeStamp(talkData.getLastMessageDate().getTime());

        if(nextHandler != null)
            nextHandler.handleMsg(sendMsgDTO,pushMessageDTO);
    }

    private boolean checkIsNewPrivateChatAndActivate(String uid, Long chatId) {
        // 2、判断是否是新会话（收到信息的用户，该用户的会话是否处于有效状态)
        boolean isNewTalk = !chatService.isPrivateChatSessionOpenToUser(uid, chatId);

        if (isNewTalk) { // 如果是新会话，更新
            redisService.hset(GlobalConst.Redis.KEY_CHAT_REMOVE, uid + chatId, false);
            new Thread(() -> {
                PrivateChat privateChat = new PrivateChat();
                privateChat.setChatId(chatId);
                privateChat.setUserAStatus(true);
                privateChat.setUserBStatus(true);
                privateChat.setUpdateTime(new Date());
                chatMapper.updatePrivateChat(privateChat);
            }).start();
        }
        return isNewTalk;
    }
}
