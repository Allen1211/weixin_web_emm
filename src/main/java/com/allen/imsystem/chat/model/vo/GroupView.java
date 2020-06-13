package com.allen.imsystem.chat.model.vo;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.user.model.vo.UserInfoView;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupView {
    private String gid;
    private String groupName;
    private String ownerId;
    private String groupAvatar = GlobalConst.Path.DEFAULT_GROUP_AVATAR;
    private List<UserInfoView> memberList = new ArrayList<>();
}
