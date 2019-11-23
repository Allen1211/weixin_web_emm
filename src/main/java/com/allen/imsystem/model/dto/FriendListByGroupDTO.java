package com.allen.imsystem.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class FriendListByGroupDTO {

    private Integer groupId;

    private String groupName;

    private Integer groupSize;

    private Boolean isDefault;

    private List<UserInfoDTO> members;
}
