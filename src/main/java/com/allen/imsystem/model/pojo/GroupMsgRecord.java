package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class GroupMsgRecord implements Cloneable{
    private Long msgId;
    private String senderId;
    private String gid;
    private Integer msgType;
    private String content;
    private String fileMd5;
    private Boolean status;
    private Date createdTime;
    private Date updateTime;

    public GroupMsgRecord() {

    }

    public GroupMsgRecord(Long msgId, String senderId, String gid, Integer msgType, String content, String fileMd5, Boolean status, Date createdTime, Date updateTime) {
        this.msgId = msgId;
        this.senderId = senderId;
        this.gid = gid;
        this.msgType = msgType;
        this.content = content;
        this.fileMd5 = fileMd5;
        this.status = status;
        this.createdTime = createdTime;
        this.updateTime = updateTime;
    }
}
