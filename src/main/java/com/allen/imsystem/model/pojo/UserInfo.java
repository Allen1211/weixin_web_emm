package com.allen.imsystem.model.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
public class UserInfo {
    private int id;
    private String uid;
    private String username;
    private String iconId;
    private int gender;
    private int age;
    private String desc;
    private int regionId;
    private Date createdTime;
    private Date updateTIme;

    public UserInfo() {
    }

    public UserInfo(String uid,String username) {
        this.uid = uid;
        this.username = username;
    }

    public static void main(String[] args) {
        UserInfo userInfo = new UserInfo();
        userInfo.getIconId();
    }
}
