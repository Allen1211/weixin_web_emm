package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class UserChatGroup {

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

    public UserChatGroup() {
    }

    public UserChatGroup(Long chatId, String uid, String gid, String userAlias, String inviterId, Boolean shouldDisplay) {
        this.chatId = chatId;
        this.uid = uid;
        this.gid = gid;
        this.userAlias = userAlias;
        this.inviterId = inviterId;
        this.shouldDisplay = shouldDisplay;
    }
}
