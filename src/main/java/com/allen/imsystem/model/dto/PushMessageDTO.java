package com.allen.imsystem.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
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
