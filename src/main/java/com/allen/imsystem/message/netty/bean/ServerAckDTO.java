package com.allen.imsystem.message.netty.bean;

import lombok.Data;

@Data
public class ServerAckDTO {
    private Long messageId;
    private Long talkId;
    private String timeStamp;
    private String lastMessage;
    private String lastMessageTime;

    public ServerAckDTO(Long talkId, Long messageId, String timeStamp) {
        this.talkId = talkId;
        this.messageId = messageId;
        this.timeStamp = timeStamp;
    }
}
