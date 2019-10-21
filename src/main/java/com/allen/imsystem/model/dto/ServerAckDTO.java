package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class ServerAckDTO {
    private Long messageId;
    private String timestamp;

    public ServerAckDTO(Long messageId, String timestamp) {
        this.messageId = messageId;
        this.timestamp = timestamp;
    }
}
