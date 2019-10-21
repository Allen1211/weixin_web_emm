package com.allen.imsystem.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChatSessionDTO implements Serializable {
    // 会话id
    private Long talkId;
    // 会话标题， 用户会话则为用户名，群会话则为群名
    private String talkTitle;
    // 会话的头像，好友会话就好友头像，群会话就是群头像
    private String avatar;
    // 最后一条消息的时间
    @JSONField(format = "yy/MM/dd")
    private Date lastMessageDate;

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
    private String groupName;
    private Long groupId;
    private String lastSenderName;



}
