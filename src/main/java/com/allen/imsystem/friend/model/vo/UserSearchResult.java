package com.allen.imsystem.friend.model.vo;

import com.allen.imsystem.user.model.vo.UserInfoView;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserSearchResult implements Serializable {

    private boolean applicable = true;

    private String reason;

    private UserInfoView userInfo;



}
