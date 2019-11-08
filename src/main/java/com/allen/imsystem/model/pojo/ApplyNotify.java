package com.allen.imsystem.model.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class ApplyNotify {
    private Integer id;
    private String uid;
    private Integer applyId;
    private Integer type;
    private Boolean status;
    private Date createdTime;
    private Date updateTime;

    public ApplyNotify() {
    }

    public ApplyNotify(String uid, Integer applyId, Integer type) {
        this.uid = uid;
        this.applyId = applyId;
        this.type = type;
    }
}
