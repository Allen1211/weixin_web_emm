package com.allen.imsystem.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSessionInfo implements Serializable {
    private Long talkId;
    private String title;
    private Boolean isGroup;
    private String srcId;
    private String destId;
    private String avatar;
    private Long lastTimeStamp;
    // 只对群有效
    private Boolean isGroupOwner = false;
    private String gid;
    private String groupAlias;



    public ChatSessionInfo() {
    }


}
