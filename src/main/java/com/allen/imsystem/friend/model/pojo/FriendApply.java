package com.allen.imsystem.friend.model.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class FriendApply {
    private Integer id;
    private String fromUid;
    private String toUid;
    private Integer groupId;
    private Boolean pass;
    private String reason;
    private Boolean hasRead;
    private Date createdTime;
    private Date updateTime;

    public FriendApply(String fromUid, String toUid, Integer groupId, String reason) {
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.groupId = groupId;
        this.reason = reason;
    }
}
