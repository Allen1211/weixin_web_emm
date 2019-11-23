package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class ClientAckDTO {
    private Long messageId;
    private Long talkId;
}
