package com.allen.imsystem.message.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class GroupMsgRecord extends BaseMsgRecord implements Cloneable{
    private String senderId;
    private String gid;

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
