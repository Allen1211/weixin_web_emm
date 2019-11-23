package com.allen.imsystem.model.dto;

import lombok.Data;

@Data
public class SendMsgDTO {
    private Long msgId;
    private Integer messageType;
    private String messageText;
    private String talkId;
    private String srcId;
    private String destId;
    private String messageImgUrl;
    private MsgFileInfo fileInfo;
    private String timeStamp;
    private Boolean isGroup;
    private String gid;
}


