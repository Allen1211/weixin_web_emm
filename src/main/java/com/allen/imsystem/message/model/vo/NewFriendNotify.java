package com.allen.imsystem.message.model.vo;

import com.allen.imsystem.user.model.vo.UserInfoView;
import lombok.Data;

@Data
public class NewFriendNotify {

    private UserInfoView friendInfo;
    private Integer groupId;

    public NewFriendNotify() {
    }

    public NewFriendNotify(UserInfoView friendInfo, Integer groupId) {
        this.friendInfo = friendInfo;
        this.groupId = groupId;
    }
}
