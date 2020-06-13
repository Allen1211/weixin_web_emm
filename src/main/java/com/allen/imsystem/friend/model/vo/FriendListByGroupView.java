package com.allen.imsystem.friend.model.vo;

import com.allen.imsystem.user.model.vo.UserInfoView;
import lombok.Data;

import java.util.List;

@Data
public class FriendListByGroupView {

    private Integer groupId;

    private String groupName;

    private Integer groupSize;

    private Boolean isDefault;

    private List<UserInfoView> members;
}
