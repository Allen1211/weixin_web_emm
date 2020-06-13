package com.allen.imsystem.message.model.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PrivateMsgRecord extends BaseMsgRecord {
    private Long chatId;
    private String fromUid;
    private String toUid;
    private String resourceUrl;
    private Boolean hasRead;

}
