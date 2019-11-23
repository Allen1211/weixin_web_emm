package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class User implements Serializable {

    private Integer id;
    private String uid;
    private String password;
    private String salt;
    private String email;
    private String tel;
    private Integer roleId;
    private Boolean status;
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

    public User(String password, String salt, String email, Date updateTime) {
        this.password = password;
        this.salt = salt;
        this.email = email;
        this.updateTime = updateTime;
    }
}
