package com.allen.imsystem.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class FriendListByGroupDTO {

    private String groupId;

    private String groupName;

    private Integer groupSize;

    private List<UserInfoDTO> members;
}
