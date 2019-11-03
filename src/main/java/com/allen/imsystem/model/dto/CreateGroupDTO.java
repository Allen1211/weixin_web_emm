package com.allen.imsystem.model.dto;

import lombok.Data;


@Data
public class CreateGroupDTO {
    private String gid;
    private String groupAvatar;
    private String groupName;
    private Long talkId;

    public CreateGroupDTO() {
    }

    public CreateGroupDTO(String gid, String groupAvatar, String groupName, Long talkId) {
        this.gid = gid;
        this.groupAvatar = groupAvatar;
        this.groupName = groupName;
        this.talkId = talkId;
    }
}
