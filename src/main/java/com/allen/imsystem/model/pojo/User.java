package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;
@Data
public class User {

    private Integer id;
    private String uid;
    private String password;
    private String salt;
    private String email;
    private String tel;
    private int roleId;
    private boolean status;
    private Date createdTime;
    private Date updateTime;

    public User() {
    }

    public User(String uid, String password, String salt, String email) {
        this.uid = uid;
        this.password = password;
        this.salt = salt;
        this.email = email;
    }
}
