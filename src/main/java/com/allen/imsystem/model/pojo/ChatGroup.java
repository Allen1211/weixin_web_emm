package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class ChatGroup {
    private String groupId;
    private String groupName;
    private String ownerId;
    private Long chatId;
    private String avatar;
    private Boolean status;
    private Date createdTime;
    private Date updateTime;
}
