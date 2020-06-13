package com.allen.imsystem.chat.model.pojo;

import com.allen.imsystem.common.Const.GlobalConst;
import lombok.Data;

import java.util.Date;


@Data
public class Group {
    private String gid;
    private String groupName;
    private String ownerId;
    private Long lastMsgId;
    private String lastMsgContent;
    private Date lastMsgCreateTime;
    private String lastSenderId;
    private String avatar = GlobalConst.Path.DEFAULT_GROUP_AVATAR;
    private Boolean status;
    private Date createdTime;
    private Date updateTime;

    public Group() {
    }



    public Group(String gid, String groupName, String ownerId) {
        this.gid = gid;
        this.groupName = groupName;
        this.ownerId = ownerId;
    }

    public void setAvatar(String avatar) {
        this.avatar = GlobalConst.Path.AVATAR_URL + avatar;
    }
}
