package com.allen.imsystem.model.dto;

import com.allen.imsystem.model.pojo.UserInfo;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserSearchResult implements Serializable {

    private boolean applicable;

    private String reason;

    private UserInfoDTO userInfo;



}
