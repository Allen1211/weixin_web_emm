package com.allen.imsystem.model.pojo;

import com.allen.imsystem.common.Const.GlobalConst;
import lombok.Data;

import java.util.Date;


@Data
public class GroupChat {
    private String gid;
    private String groupName;
    private String ownerId;
    private Long lastMsgId;
    private String lastSenderId;
    private String avatar = GlobalConst.Path.DEFAULT_GROUP_AVATAR;
    private Boolean status;
    private Date createdTime;
    private Date updateTime;

    public GroupChat() {
    }



    public GroupChat(String gid, String groupName, String ownerId) {
        this.gid = gid;
        this.groupName = groupName;
        this.ownerId = ownerId;
    }
}
