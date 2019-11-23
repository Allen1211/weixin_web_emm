package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class FriendApplicationDTO {

    private String applicationReason;

    private Boolean hasAccept;

    private UserInfoDTO applicantInfo;

    public FriendApplicationDTO() {
    }

    public FriendApplicationDTO(String applicationReason, Boolean hasAccept, UserInfoDTO applicantInfo) {
        this.applicationReason = applicationReason;
        this.hasAccept = hasAccept;
        this.applicantInfo = applicantInfo;
    }
}
