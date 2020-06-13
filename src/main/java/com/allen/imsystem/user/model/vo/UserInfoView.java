package com.allen.imsystem.user.model.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class UserInfoView {

    private String uid;

    private String username;

    private String signWord;

    private String avatar;

    @JSONField(format = "yyyy-MM-dd HH:mm")
    private Date lastLoginAt;

    private Boolean online;

}
