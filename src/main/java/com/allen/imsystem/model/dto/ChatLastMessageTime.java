package com.allen.imsystem.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ChatLastMessageTime {
    private Long chatId;
    private java.sql.Timestamp lastMsgTime;

}
