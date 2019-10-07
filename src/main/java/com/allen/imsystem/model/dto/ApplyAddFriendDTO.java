package com.allen.imsystem.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class ApplyAddFriendDTO {
    @NotEmpty
    private String uid;
    @NotEmpty
    private String friendId;

    private String applicationReason;

    private String groupId;
}
