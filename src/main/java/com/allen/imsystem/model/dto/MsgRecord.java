package com.allen.imsystem.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class MsgRecord {
    @JSONField(serializeUsing= ToStringSerializer.class)
    private Long messageId;         // 消息id
    private Integer userType;       // 0->其他人发的，1->自己发的
    private Integer msgType;        // 消息类型
    private String messageText;     // 消息内容
    private String messageImgUrl;   // 图片url
    private MsgFileInfo fileInfo;   // 文件信息
    private UserInfoDTO userInfo;   // 发送者用户信息
    @JSONField(format = "yy/MM/dd HH:mm")
    private Date msgTimeDate;         // 未处理前的发送时间
    private String msgTime;           // 处理后的发送时分
    private Boolean showMsgTime;    // 是否显示时间(两条信息超过一定时间才显示)
    private Boolean showMsg;        // 是否显示该信息
    private String groupAlias;      // 发送者群昵称
}
