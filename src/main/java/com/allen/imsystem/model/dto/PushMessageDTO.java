package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class PushMessageDTO {
    private Long talkId;
    private MsgRecord messageData;
    private Boolean isNewTalk;
    private ChatSessionDTO talkData;
    private Long lastTimeStamp;
}
