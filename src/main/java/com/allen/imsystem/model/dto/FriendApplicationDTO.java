package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class FriendApplicationDTO {

    private String applicationReason;

    private Boolean hasAccept;

    private UserInfoDTO applicationInfo;
}
