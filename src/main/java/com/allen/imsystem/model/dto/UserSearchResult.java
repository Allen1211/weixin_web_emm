package com.allen.imsystem.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSearchResult implements Serializable {

    private boolean applicable = true;

    private String reason;

    private UserInfoDTO userInfo;



}
