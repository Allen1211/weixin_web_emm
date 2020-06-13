package com.allen.imsystem.friend.model.vo;

import com.allen.imsystem.user.model.vo.UserInfoView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class FriendApplicationView {

    private String applicationReason;

    private Boolean hasAccept;

    @JsonIgnore
    private String fromUid;

    private UserInfoView applicantInfo;

    public FriendApplicationView() {
    }

    public FriendApplicationView(String applicationReason, Boolean hasAccept, UserInfoView applicantInfo) {
        this.applicationReason = applicationReason;
        this.hasAccept = hasAccept;
        this.applicantInfo = applicantInfo;
    }
}
