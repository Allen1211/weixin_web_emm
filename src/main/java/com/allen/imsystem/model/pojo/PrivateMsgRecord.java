package com.allen.imsystem.model.pojo;

import lombok.Data;

@Data
public class PrivateMsgRecord extends BaseMsgRecord {
    private Long chatId;
    private String fromUid;
    private String toUid;
    private String resourceUrl;
    private Boolean hasRead;

}
