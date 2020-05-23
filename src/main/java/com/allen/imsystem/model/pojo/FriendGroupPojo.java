package com.allen.imsystem.model.pojo;

import lombok.Data;

@Data
public class FriendGroupPojo {
    private Integer groupId;
    private String friendGroupName;
    private String uid;
    private Integer size;
    private Boolean status;
    private Boolean isDefault;

    public FriendGroupPojo() {
    }

    public FriendGroupPojo(String friendGroupName, String uid, Integer size, Boolean status, Boolean isDefault) {
        this.friendGroupName = friendGroupName;
        this.uid = uid;
        this.size = size;
        this.status = status;
        this.isDefault = isDefault;
    }
}
