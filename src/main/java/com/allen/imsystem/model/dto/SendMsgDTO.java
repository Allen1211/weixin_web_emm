package com.allen.imsystem.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class SendMsgDTO {
    private Long msgId;
    private Integer messageType;
    private String messageText;
    private String talkId;
    private String srcId;
    private String destId;
    private String messageImgData;
    private MsgFileInfo fileInfo;
    private String timestamp;
    private Boolean isGroup;
}
