package com.allen.imsystem.model.dto;

import com.allen.imsystem.common.Const.GlobalConst;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupChatInfoDTO {
    private String gid;
    private String groupName;
    private String ownerId;
    private String groupAvatar = GlobalConst.Path.DEFAULT_GROUP_AVATAR;
    private List<UserInfoDTO> memberList = new ArrayList<>();
}
