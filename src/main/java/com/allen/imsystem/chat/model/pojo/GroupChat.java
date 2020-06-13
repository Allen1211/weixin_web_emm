package com.allen.imsystem.chat.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class GroupChat {

    private Long chatId;
    private String uid;
    private String gid;
    private Long lastAckMsgId;
    private String userAlias;
    private String inviterId;
    private Boolean status;
    private Boolean shouldDisplay;
    private Date createdTime;
    private Date updateTime;

    public GroupChat() {
    }

    public GroupChat(Long chatId, String uid, String gid, String userAlias, String inviterId, Boolean shouldDisplay) {
        this.chatId = chatId;
        this.uid = uid;
        this.gid = gid;
        this.userAlias = userAlias;
        this.inviterId = inviterId;
        this.shouldDisplay = shouldDisplay;
    }

    public GroupChat(Long chatId, String uid, String gid, Long lastAckMsgId, String userAlias, String inviterId, Boolean status, Boolean shouldDisplay, Date createdTime, Date updateTime) {
        this.chatId = chatId;
        this.uid = uid;
        this.gid = gid;
        this.lastAckMsgId = lastAckMsgId;
        this.userAlias = userAlias;
        this.inviterId = inviterId;
        this.status = status;
        this.shouldDisplay = shouldDisplay;
        this.createdTime = createdTime;
        this.updateTime = updateTime;
    }
}
