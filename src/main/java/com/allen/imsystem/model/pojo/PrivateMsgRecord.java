package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class PrivateMsgRecord {
    private Long msgId;
    private Integer msgType;
    private Long chatId;
    private String fromUid;
    private String toUid;
    private String content;
    private Boolean hasRead;
    private Integer status;
    private Date createdTime;
    private Date updateTime;
}
