package com.allen.imsystem.message.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.allen.imsystem.file.model.MsgFileInfo;
import com.allen.imsystem.user.model.vo.UserInfoView;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@ApiModel(description = "聊天记录数据对象")
@Data
public class MsgRecord {
    @JSONField(serializeUsing= ToStringSerializer.class)
    private Long messageId;         // 消息id
    private Integer userType;       // 0->其他人发的，1->自己发的
    private Integer messageType;        // 消息类型
    private String messageText;     // 消息内容
    private String messageImgUrl;   // 图片url
    private MsgFileInfo fileInfo;   // 文件信息
    private UserInfoView userInfo;   // 发送者用户信息
    @JSONField(serialize = false)
    private Date msgTimeDate;         // 未处理前的发送时间
    private String messageTime;           // 处理后的发送时分
    private Boolean showMessageTime;    // 是否显示时间(两条信息超过一定时间才显示)
    private Boolean showMessage;        // 是否显示该信息
    private String groupAlias;      // 发送者群昵称
    private String fromUid;        // 发送者uid
}
