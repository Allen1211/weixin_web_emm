package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class FriendInfoForInvite {
    private UserInfoDTO friendInfo;
    private Boolean canInvite;
    private String reason;
}
