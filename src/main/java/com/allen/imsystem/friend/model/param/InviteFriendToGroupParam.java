package com.allen.imsystem.friend.model.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName InviteFriendToGroupParam
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/12
 * @Version 1.0
 */
@Data
public class InviteFriendToGroupParam {

    private String gid;

    @JsonProperty("friendList")
    private List<InviteParam> inviteFriendList;
}
