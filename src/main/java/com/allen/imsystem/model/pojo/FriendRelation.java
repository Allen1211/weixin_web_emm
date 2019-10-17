package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FriendRelation implements Serializable {
    private Integer id;
    private String uidA;
    private String uidB;
    private Integer aInbGroupId;
    private Integer bInaGroupId;
    private Boolean aDeleteB;
    private Boolean bDeleteA;
    private Date createdTime;
    private Date updateTime;
}
