package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class NewFriendNotify {

    private UserInfoDTO friendInfo;
    private Integer groupId;

    public NewFriendNotify() {
    }

    public NewFriendNotify(UserInfoDTO friendInfo, Integer groupId) {
        this.friendInfo = friendInfo;
        this.groupId = groupId;
    }
}
