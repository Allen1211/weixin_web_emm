package com.allen.imsystem.message.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.allen.imsystem.chat.model.vo.ChatSession;
import com.allen.imsystem.message.model.vo.MsgRecord;
import lombok.Data;

@Data
public class PushMessageDTO {
    @JSONField(name = "talkId")
    private Long chatId;
    private MsgRecord messageData;
    private Boolean isNewTalk;
    private ChatSession talkData;
    private Long lastTimeStamp;

    @JSONField(name = "talkId")
    public Long getChatId() {
        return chatId;
    }
}
