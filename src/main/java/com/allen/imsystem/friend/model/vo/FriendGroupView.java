package com.allen.imsystem.friend.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FriendGroupView {

    private Integer groupId;

    private String groupName;

    private Integer groupSize;

    private Boolean isDefault;

    private Boolean status;

    List list = new ArrayList<>();

}
