package com.allen.imsystem.friend.model.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * 申请添加好友的bean
 */
@Data
public class ApplyAddFriendParam {
    @NotNull
    private String friendId;

    private String applicationReason;

    private String groupId;
}
