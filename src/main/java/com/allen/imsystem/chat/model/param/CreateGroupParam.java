package com.allen.imsystem.chat.model.param;

import lombok.Data;


@Data
public class CreateGroupParam {
    private String gid;
    private String groupAvatar;
    private String groupName;
    private Long talkId;

    public CreateGroupParam() {
    }

    public CreateGroupParam(String gid, String groupAvatar, String groupName, Long talkId) {
        this.gid = gid;
        this.groupAvatar = groupAvatar;
        this.groupName = groupName;
        this.talkId = talkId;
    }
}
