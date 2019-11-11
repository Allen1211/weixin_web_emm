package com.allen.imsystem.model.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * 申请添加好友的bean
 */
@Data
public class ApplyAddFriendDTO {
    @NotEmpty
    private String friendId;

    private String applicationReason;

    private String groupId;
}
