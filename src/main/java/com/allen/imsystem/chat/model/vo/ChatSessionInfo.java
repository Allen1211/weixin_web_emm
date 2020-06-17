package com.allen.imsystem.chat.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChatSessionInfo implements Serializable {
    private Long chatId;
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

    @JsonIgnore
    private boolean isOpen;

    public ChatSessionInfo() {
    }

    @JSONField(name = "talkId")
    public Long getChatId() {
        return chatId;
    }
}
