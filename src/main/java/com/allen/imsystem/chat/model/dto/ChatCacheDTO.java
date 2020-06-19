package com.allen.imsystem.chat.model.dto;

import lombok.Data;

/**
 * @ClassName ChatCacheDTO
 * @Description
 * @Author XianChuLun
 * @Date 2020/6/18
 * @Version 1.0
 */
@Data
public class ChatCacheDTO {

    private Long chatId;

    private Long lastMsgTimestamp;

    private Integer unreadMsgCount;

    private Boolean shouldDisplay;

    private boolean isGroup;

    private String gid;
}
