package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class PrivateChat implements Serializable {
    /**
     * 会话id
     */
    private Long chatId;

    /**
     * 用户a uid 与b相比较小
     */
    private String uidA;

    /**
     * 用户b
     */
    private String uidB;

    /**
     * 对用户a是否有效
     */
    private Boolean userAStatus = false;

    /**
     * 对用户b是否有效
     */
    private Boolean userBStatus = false;

    /**
     * 最后一条消息的发送者uid
     */
    private String lastSenderId;

    /**
     * 最后一条消息id
     */
    private Long lastMsgId;

    /**
     * 最后一条消息的内容
     */
    private String lastMsgContent;

    /**
     * 最后一条消息的创建时间
     */
    private Date lastMsgCreateTime;

    private Date createdTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

}
