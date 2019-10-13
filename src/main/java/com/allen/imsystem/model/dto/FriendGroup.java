package com.allen.imsystem.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FriendGroup {

    private String groupId;

    private String groupName;

    private Integer groupSize;

    List list = new ArrayList<>();

}
