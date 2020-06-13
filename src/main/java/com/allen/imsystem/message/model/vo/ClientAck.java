package com.allen.imsystem.message.model.vo;

import lombok.Data;

@Data
public class ClientAck {
    private Long messageId;
    private Long talkId;
}
