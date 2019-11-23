package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfo {
    private Integer id;
    private String uid;
    private String username;
    private String iconId;
    private Integer gender;
    private Integer age;
    private String desc;
    private Integer regionId;
    private Date createdTime;
    private Date updateTime;

    public UserInfo() {
    }

    public UserInfo(String uid,String username) {
        this.uid = uid;
        this.username = username;
    }
}
