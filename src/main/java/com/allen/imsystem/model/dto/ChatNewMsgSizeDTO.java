package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class ChatNewMsgSizeDTO {
    // 会话id
    private Long talkId;
    // 新消息条数
    private int size;

}
