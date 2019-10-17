package com.allen.imsystem.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TalkInfo implements Serializable {
    private Long talkId;
    private String title;
    private Boolean isGroup;
    // 只对群有效
    private Boolean isGroupOwner = false;
}
