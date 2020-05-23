package com.allen.imsystem.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.allen.imsystem.common.utils.FormatUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChatSessionDTO implements Serializable {
    // 会话id
    @JsonProperty(value = "talkId")
    private Long chatId;
    // 会话标题， 用户会话则为用户名，群会话则为群名
    private String talkTitle;
    // 会话的头像，好友会话就好友头像，群会话就是群头像
    private String avatar;

    // 最后一条消息的时间,前端显示的字符串
    private String lastMessageTime;

    // 最后一条消息内容， 如果是图片则为 [图片], 文件则为 [文件]
    private String lastMessage;
    //新消息条数
    private Integer newMessageCount;
    // 是否是群
    private Boolean isGroupChat;

    // 以下字段好友私聊特有
    private Boolean online;
    private String friendId;

    // 以下字段是群会话特有，
    private String gid;
    private String lastSenderName;


    @JSONField(serialize = false)
    private Date lastMessageDate;
    @JSONField(serialize = false)
    private String myId;
    @JSONField(serialize = false)
    private Date updateTime;

    @JSONField(name = "talkId")
    public Long getChatId() {
        return chatId;
    }

    /**
     * 数据库查出来注入lastMessageDate的时候，自动生成格式化的消息时间
     */
    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
        if(lastMessageDate != null){
            this.lastMessageTime = FormatUtil.formatChatSessionDate(lastMessageDate);
        }
    }
}
