package com.allen.imsystem.friend.model.vo;

import com.allen.imsystem.user.model.vo.UserInfoView;
import lombok.Data;

@Data
public class FriendInfoForInvite {
    private UserInfoView friendInfo;
    private Boolean canInvite;
    private String reason;
}
