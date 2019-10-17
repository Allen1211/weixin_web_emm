package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class PrivateChat {
    private Long chatId;
    private String uidA;
    private String uidB;
    private Boolean userAStatus;
    private Boolean userBStatus;
    private Long lastMsgId;
    private String lastSenderId;
    private Date createdTime;
    private Date updateTime;

}
