package com.allen.imsystem.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class FriendApplicationDTO {

    private String applicationReason;

    private Boolean hasAccept;

    @JsonIgnore
    private String fromUid;

    private UserInfoDTO applicantInfo;

    public FriendApplicationDTO() {
    }

    public FriendApplicationDTO(String applicationReason, Boolean hasAccept, UserInfoDTO applicantInfo) {
        this.applicationReason = applicationReason;
        this.hasAccept = hasAccept;
        this.applicantInfo = applicantInfo;
    }
}
